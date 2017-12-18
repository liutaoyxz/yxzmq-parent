package com.liutaoyxz.yxzmq.broker.storage;

import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
    public static void setQueueListeners(String queueName, BlockingQueue<ServerClient> listeners) {
        if (StringUtils.isBlank(queueName)) {
            return;
        }
        checkQueue(queueName);
        log.info("set queue listeners,queue name is [{}] , listeners is {}", queueName, listeners);
        BlockingQueue<ServerClient> ls = QUEUE_LISTENERS.get(queueName);
        BlockingQueue<ServerClient> fls = FIXED_QUEUE_LISTENERS.get(queueName);
        ls.clear();
        fls.clear();
        ls.addAll(listeners);
        fls.addAll(listeners);
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
                FIXED_QUEUE_LISTENERS.put(queueName, clients);
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
