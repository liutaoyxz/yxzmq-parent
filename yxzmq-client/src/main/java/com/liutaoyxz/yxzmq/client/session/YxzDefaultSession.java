package com.liutaoyxz.yxzmq.client.session;

import com.liutaoyxz.yxzmq.client.connection.YxzDefaultConnection;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.*;

/**
 * @author Doug Tao
 * @Date 上午11:10 2017/11/19
 * @Description:
 */
public class YxzDefaultSession extends AbstractSession {

    public static final Logger log = LoggerFactory.getLogger(YxzDefaultSession.class);

    private YxzDefaultConnection connection;

    private MessageListener messageListener;

    /**
     * 是否关闭
     */
    private boolean closed;

    private CopyOnWriteArrayList<MessageConsumer> topicConsumers = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<MessageProducer> producers = new CopyOnWriteArrayList<>();
    /**
     * 任务
     */
    private LinkedBlockingQueue<YxzSessionTask> tasks = new LinkedBlockingQueue<>();

    private ExecutorService executor;


    public YxzDefaultSession(YxzDefaultConnection connection) {
        this.connection = connection;
        this.executor = new ThreadPoolExecutor(1,1,5, TimeUnit.SECONDS,new LinkedBlockingQueue());
    }

    @Override
    public void close() throws JMSException {

    }

    /**
     * 任务
     */
    @Override
    public void run() {
        try {
            log.debug("session start task");
            while (!closed){
                Runnable task = tasks.take();
                executor.execute(task);
            }
            log.debug("session closed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向session中添加一个任务,messageProducer/messageConsumer提供
     * @param task
     */
    void addTask(YxzSessionTask task){
        this.tasks.add(task);
    }

    /**
     * 获得消息监听器
     * @return
     * @throws JMSException
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        return this.messageListener;
    }

    /**
     * 设置消息监听器
     * @param messageListener session 级别的监听器
     * @throws JMSException
     */
    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {
        this.messageListener = messageListener;
    }

    /**
     * 创建一个主题的queue
     * @param queueName 主题
     * @return
     * @throws JMSException
     */
    @Override
    public Queue createQueue(String queueName) throws JMSException {
        if (StringUtils.isBlank(queueName)){
            throw new NullPointerException("queueName can not be null");
        }
        return new YxzQueue(queueName);
    }

    /**
     * 创建一个主题
     * @param topicName
     * @return
     * @throws JMSException
     */
    @Override
    public Topic createTopic(String topicName) throws JMSException {
        if (StringUtils.isBlank(topicName)){
            throw new NullPointerException("topicName can not be null");
        }
        return new YxzTopic(topicName);
    }

    /**
     * 创建一个订阅者
     * @param topic
     * @param name
     * @return
     * @throws JMSException
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        return null;
    }

    /**
     * 创建一个字符串的消息
     * @param text 消息
     * @return
     * @throws JMSException
     */
    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        if(StringUtils.isBlank(text)){
            throw new NullPointerException();
        }
        return new YxzTextMessage(text);
    }


    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        if (closed){
            throw JMSErrorEnum.SESSION_CLOSED.exception();
        }
        if (destination == null){
            throw new NullPointerException("destination can not be null");
        }
        /**
         * 主题模式的地址
         * todo queue 暂时不做
         */
        if (destination instanceof Topic){
            YxzDefaultTopicPublisher publisher = new YxzDefaultTopicPublisher((Topic) destination,this);
            return publisher;
        }
        YxzDefaultQueueSender sender = new YxzDefaultQueueSender(this,(Queue)destination);
        return sender;
    }


    /**
     * 创建一个消费者,消费者创建的时候就注册到broker
     * @param destination
     * @return
     * @throws JMSException
     */
    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        if (closed){
            throw JMSErrorEnum.SESSION_CLOSED.exception();
        }
        if (destination == null){
            throw new NullPointerException("destination can not be null");
        }
        /**
         * 主题模式的地址
         * todo queue 暂时不做
         */
        if (destination instanceof Topic){
            YxzDefaultTopicSubscriber topicSubscriber = new YxzDefaultTopicSubscriber((Topic) destination,this);
            this.topicConsumers.add(topicSubscriber);
            return topicSubscriber;
        }
        YxzQueueConsumer consumer = new YxzQueueConsumer((Queue) destination,this);
        return consumer;
    }

    YxzDefaultConnection getConnection(){
        return this.connection;
    }


    /***************************事务处理****************************/

    @Override
    public boolean getTransacted() throws JMSException {
        return false;
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return 0;
    }

    @Override
    public void commit() throws JMSException {

    }

    @Override
    public void rollback() throws JMSException {

    }

    @Override
    public void recover() throws JMSException {

    }

    /***************************事务处理****************************/
}
