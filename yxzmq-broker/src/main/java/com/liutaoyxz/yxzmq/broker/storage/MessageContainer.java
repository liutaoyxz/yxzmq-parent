package com.liutaoyxz.yxzmq.broker.storage;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.Group;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import com.liutaoyxz.yxzmq.io.wrap.TopicMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 17:05 2017/11/17
 * @Description: 消息容器,保存消息/客户端, 消息内容 等信息
 */
public class MessageContainer {

    /**
     * 主题消息的数量
     */
    private static final ConcurrentHashMap<String,AtomicInteger> TOPIC_COUNT = new ConcurrentHashMap<>();

    /**
     * 主题和对应的Group
     */
    private static final ConcurrentHashMap<String,CopyOnWriteArrayList<Group>> TOPIC_GROUP = new ConcurrentHashMap<>();

    private static ReentrantLock topicLock = new ReentrantLock();

    /**
     * 点对点模式,消息队列
     * queueName 和 queueMessage 对应的映射
     */
    private static final ConcurrentHashMap<String,BlockingDeque<QueueMessage>> PP_HOUSE  = new ConcurrentHashMap<>();

    /**
     * queueName 和group 对应的映射
     */
    private static final ConcurrentHashMap<String,CopyOnWriteArrayList<Group>> QNAME_GROUP = new ConcurrentHashMap<>();

    /**
     * queueName 和 condition 映射
     */
    private static final ConcurrentHashMap<String,Condition> QNAME_CONDITION = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String,ReentrantLock> QNAME_LOCK = new ConcurrentHashMap<>();

    /**
     * 队列是否取消
     */
    public static final ConcurrentHashMap<String,AtomicBoolean> QNAME_CANCEL = new ConcurrentHashMap<>();

    /**
     * queue 操作的lock
     */
    public static ReentrantLock queueLock = new ReentrantLock();

    /**
     * queue 监听线程池,每一个主题的queue会对应一个线程,线程会在获取queue的过程中阻塞
     */
    private static final ExecutorService QUEUE_TASK = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("queue_listen_task");
            return thread;
        }
    });

    private static final Logger log = LoggerFactory.getLogger(MessageContainer.class);

    /**
     * 保存主题消息
     * @param title 主题
     * @param message 主题消息包装类
     * @return 保存是否成功
     * TODO 主题消息不应该保存,占用存储空间. 最后改造成为保存到日志中,在持久化中完成这个功能
     */
    public static boolean save(String topicName,TopicMessage message) throws IOException {
        checkTitle(topicName);
        AtomicInteger count = TOPIC_COUNT.get(topicName);
        //计数
        if (count == null){
            TOPIC_COUNT.put(topicName,new AtomicInteger(1));
        }else {
            count.getAndIncrement();
        }
        //分发
        CopyOnWriteArrayList<Group> groups = TOPIC_GROUP.get(topicName);
        if (groups == null){
            //没有订阅者
            return true;
        }
        for (Group g : groups){
            if (!g.isAlive()){
                groups.remove(g);
                continue;
            }
            Client client = g.applyClient();
            try {
                if (client != null){
                    List<byte[]> bytes = getTopicMessageByte(topicName,message.getText(),g.groupId());
                    SocketChannel channel = client.channel();
                    for (byte[] b : bytes){
                        ByteBuffer buffer = ByteBuffer.wrap(b);
                        channel.write(buffer);
                        while (buffer.hasRemaining()){
                            channel.write(buffer);
                        }
                    }
                }else if(!g.isAlive()){
                    groups.remove(g);
                }
            }finally {
                g.returnClient(client);
            }

        }
        return true;
    }

    /**
     * 生成主题消息的字节
     * @param topicName
     * @param text
     * @return
     */
    private static List<byte[]> getTopicMessageByte(String topicName,String text,String groupId){
        MessageDesc desc = new MessageDesc();
        desc.setTitle(topicName);
        desc.setType(CommonConstant.MessageType.TOPIC);

        ProtocolBean bean = new ProtocolBean();
        bean.setDataBytes(text.getBytes(Charset.forName(ReadContainer.DEFAULT_CHARSET)));
        bean.setCommand(CommonConstant.Command.SEND);
        bean.setGroupId(groupId);

        Metadata metadata = new Metadata();
        return BeanUtil.convertBeanToByte(metadata,desc,bean);
    }



    /**
     * 订阅
     * @param topicName
     * @param group
     */
    public static void subscribe(String topicName,Group group){
        topicLock.lock();
        try {
            CopyOnWriteArrayList<Group> groups = TOPIC_GROUP.get(topicName);
            if (groups == null){
                groups = new CopyOnWriteArrayList<>();
                groups.add(group);
                TOPIC_GROUP.put(topicName,groups);
                return ;
            }
            if (!groups.contains(group)){
                groups.add(group);
                TOPIC_GROUP.put(topicName,groups);
                return ;
            }
        }finally {
            topicLock.unlock();
        }
    }

    /**
     * 监听queue
     * @param queueName
     * @param group
     */
    public static void listenQueue(String queueName,Group group){
        checkQueue(queueName);
        CopyOnWriteArrayList<Group> groups = QNAME_GROUP.get(queueName);
        groups.add(group);
        Condition c = QNAME_CONDITION.get(queueName);
        ReentrantLock lock = QNAME_LOCK.get(queueName);
        lock.lock();
        try {
            c.signal();
        }finally {
            lock.unlock();
        }
    }

    private static void checkQueue(String queueName){
        queueLock.lock();
        try {
            BlockingDeque<QueueMessage> messages = PP_HOUSE.get(queueName);
            if (messages == null){
                messages = new LinkedBlockingDeque<>();
                PP_HOUSE.put(queueName,messages);
                CopyOnWriteArrayList groups = new CopyOnWriteArrayList<>();
                QNAME_GROUP.put(queueName,groups);
                AtomicBoolean cancel = new AtomicBoolean(false);
                QNAME_CANCEL.put(queueName,cancel);
                QueueListenTask task = new QueueListenTask(messages,groups,cancel);
                QUEUE_TASK.execute(task);

                Condition condition = task.getCondition();
                QNAME_CONDITION.put(queueName,condition);

                ReentrantLock lock = task.getLock();
                QNAME_LOCK.put(queueName,lock);

            }
        }finally {
            queueLock.unlock();
        }

    }

    public static List<Group> getGroups(String queueName){
        return QNAME_GROUP.get(queueName);
    }

    public static QueueMessage takeQueueMessage(String queueName){
        try {
            return PP_HOUSE.get(queueName).take();
        } catch (InterruptedException e) {
            log.debug("takeQueueMessage error",e);
        }
        return null;
    }


    /**
     * 保存p2p消息
     * @param title 主题
     * @param message 消息包装类
     * @return 保存是否成功
     */
    public static boolean save(String title,QueueMessage message){
        checkTitle(title);
        checkQueue(title);
        BlockingDeque<QueueMessage> queue = PP_HOUSE.get(title);
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            log.debug("save message error",e);
            return false;
        }
        log.debug("receive message {}",message);
        return true;
    }

    private static void checkTitle(String title){
        if (StringUtils.isBlank(title)){
            throw new NullPointerException("message title can not be blank");
        }
    }

}
