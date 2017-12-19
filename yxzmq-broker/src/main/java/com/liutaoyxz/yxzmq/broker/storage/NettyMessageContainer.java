package com.liutaoyxz.yxzmq.broker.storage;

import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.broker.client.ServerClientManager;
import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 15:31 2017/12/8
 * @Description: 保存queue 消息,和 客户端和主题 的对应关系
 */
public class NettyMessageContainer {


    private static final Logger log = LoggerFactory.getLogger(NettyMessageContainer.class);

    /**
     * queue 消息的订阅者
     */
    private static final ConcurrentHashMap<String, BlockingQueue<ServerClient>> QUEUE_LISTENERS = new ConcurrentHashMap<>();

    /**
     * 固定不变的 queue 和 listeners 的映射
     */
    private static final ConcurrentHashMap<String, BlockingQueue<ServerClient>> FIXED_QUEUE_LISTENERS = new ConcurrentHashMap<>();

    /**
     * 准备连接的 queue listener
     */
    private static final ConcurrentHashMap<String,Set<String>> PREPARE_QUEUE = new ConcurrentHashMap<>();

    /**
     * topic 订阅者
     */
    private static final ConcurrentHashMap<String, List<ServerClient>> TOPIC_SUBSCRIBERS = new ConcurrentHashMap<>();

    /**
     * 队列是否取消
     */
    public static final ConcurrentHashMap<String, AtomicBoolean> QNAME_CANCEL = new ConcurrentHashMap<>();

    /**
     * queue 操作的lock
     */
    public static ReentrantLock queueLock = new ReentrantLock();

    /**
     * 点对点模式,消息队列
     * queueName 和 queueMessage 对应的映射
     */
    private static final ConcurrentHashMap<String, BlockingDeque<QueueMessage>> PP_HOUSE = new ConcurrentHashMap<>();


    /**
     * queue 监听线程池,每一个主题的queue会对应一个线程,线程会在获取queue的过程中阻塞
     */
    private static final ExecutorService QUEUE_EXECUTOR = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("queue_listen_task");
            return thread;
        }
    });

    /**
     * 设置 某一主题的订阅者
     *
     * @param topicName
     * @param subscribers
     */
    public static void setTopicSubscribers(String topicName, List<ServerClient> subscribers) {
        if (StringUtils.isBlank(topicName)) {
            return;
        }
        log.info("set topic subscribers,queue name is [{}] , subscribers is {}", topicName, subscribers);
        TOPIC_SUBSCRIBERS.put(topicName, subscribers);
    }

    /**
     * 设置某一 name下的 queue 监听者
     *
     * @param queueName
     * @param listeners
     */
    public synchronized static void setQueueListeners(String queueName, List<String> listeners) {
        if (StringUtils.isBlank(queueName)) {
            return;
        }
        checkQueue(queueName);
        log.info("set queue listeners,queue name is [{}] , listeners is {}", queueName, listeners);
        List<ServerClient> clients = new ArrayList<>();
        for (String name : listeners) {
            if (StringUtils.isNotBlank(name)){
                ServerClient sc = ServerClientManager.getServerClientByName(name);
                if (sc == null){
                    //这个连接可能还没有连接过来
                    Set<String> queueNames = PREPARE_QUEUE.get(name);
                    if (queueNames == null){
                        queueNames = new ConcurrentSet<>();
                    }
                    queueNames.add(queueName);
                    PREPARE_QUEUE.put(name,queueNames);
                    log.info("client [{}] for queue [{}] is not ready",name,queueName);
                }else {
                    //这个连接可用
                    clients.add(sc);
                }
            }
        }
        BlockingQueue<ServerClient> ls = QUEUE_LISTENERS.get(queueName);
        BlockingQueue<ServerClient> fls = FIXED_QUEUE_LISTENERS.get(queueName);
        ls.clear();
        fls.clear();
        ls.addAll(clients);
        fls.addAll(clients);
    }

    /**
     * client 准备好了,把准备列表里的都拿出来
     * @param name
     */
    public static void clientReady(String name){
        if (StringUtils.isBlank(name)){
            return;
        }
        ServerClient client = ServerClientManager.getServerClientByName(name);
        if (client == null){
            return;
        }
        Set<String> queues = PREPARE_QUEUE.get(name);
        if (queues != null && !queues.isEmpty()){
            for (String qName : queues){
                if (StringUtils.isNotBlank(qName)){
                    checkQueue(qName);
                    BlockingQueue<ServerClient> scs = QUEUE_LISTENERS.get(qName);
                    BlockingQueue<ServerClient> fscs = FIXED_QUEUE_LISTENERS.get(qName);
                    scs.add(client);
                    fscs.add(client);
                    queues.remove(qName);
                    log.info("client [{}] for queue [{}] ready",name,qName);
                }
            }
        }
    }


    private static void checkQueue(String queueName) {
        queueLock.lock();
        try {
            BlockingDeque<QueueMessage> messages = PP_HOUSE.get(queueName);
            if (messages == null) {
                messages = new LinkedBlockingDeque<>();
                PP_HOUSE.put(queueName, messages);
                BlockingDeque<ServerClient> clients = new LinkedBlockingDeque<>();
                QUEUE_LISTENERS.put(queueName, clients);
                BlockingDeque<ServerClient> fixedClients = new LinkedBlockingDeque<>();
                FIXED_QUEUE_LISTENERS.put(queueName, fixedClients);
                AtomicBoolean cancel = new AtomicBoolean(false);
                QNAME_CANCEL.put(queueName, cancel);
                NettyQueueListenTask task = new NettyQueueListenTask(messages, clients, cancel);
                QUEUE_EXECUTOR.execute(task);
            }
        } finally {
            queueLock.unlock();
        }

    }

    public static void saveQueueMessage(String queueName, QueueMessage message) {
        checkQueue(queueName);
        BlockingDeque<QueueMessage> queue = PP_HOUSE.get(queueName);
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            log.error("save message error", e);
        }
        log.debug("receive message {}", message);
    }

    public static boolean checkQueueListener(String queueName,ServerClient client){
        if (StringUtils.isBlank(queueName) || !client.available()){
            return false;
        }
        BlockingQueue<ServerClient> cs = FIXED_QUEUE_LISTENERS.get(queueName);
        if (cs == null || cs.isEmpty()){
            return false;
        }
        return cs.contains(client);
    }

}
