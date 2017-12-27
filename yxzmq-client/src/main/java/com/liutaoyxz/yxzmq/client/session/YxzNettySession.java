package com.liutaoyxz.yxzmq.client.session;

import com.liutaoyxz.yxzmq.client.YxzClientContext;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 16:00 2017/12/13
 * @Description:
 */
public class YxzNettySession extends AbstractSession {

    private static final Logger log = LoggerFactory.getLogger(YxzNettySession.class);

    /**
     * 上下文对象
     **/
    private YxzClientContext ctx;

    private boolean transacted;

    private int acknowledgeMode;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * topic listeners
     **/
    private ConcurrentHashMap<String, List<MessageListener>> topicListeners = new ConcurrentHashMap<>();

    /**
     * queue listeners
     **/
    private ConcurrentHashMap<String, BlockingDeque<MessageListener>> queueListeners = new ConcurrentHashMap<>();

    /**
     * session 关闭标识,只是标记session关闭的时刻,标识被识别后不应该再添加任务,session需要清理所有有关的信息
     */
    private volatile boolean closed = false;

    private BlockingQueue<SessionTask> tasks;

    public YxzNettySession(YxzClientContext ctx, boolean transacted, int acknowledgeMode) {
        this.ctx = ctx;
        this.transacted = transacted;
        this.acknowledgeMode = acknowledgeMode;
        this.tasks = new LinkedBlockingQueue<>();
    }


    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        if (StringUtils.isBlank(text)){
            throw new NullPointerException();
        }
        return new YxzDefaultTextMessage(text);
    }

    /****************************** transaction *********************************/
    @Override
    public boolean getTransacted() throws JMSException {
        return transacted;
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return acknowledgeMode;
    }

    @Override
    public void commit() throws JMSException {

    }

    @Override
    public void rollback() throws JMSException {

    }

    /****************************** transaction *********************************/

    @Override
    public void close() throws JMSException {
        lock.lock();
        try {
            SessionTask closeTask = new SessionTask(SessionTask.SESSION_CLOSE);
            addTask(closeTask);
            this.closed = true;
            log.info("session closed");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查session 是否关闭,必须加锁,需要与发布任务还有关闭session 操作互斥
     *
     * @return
     * @throws JMSException
     */
    private boolean checkClose() throws JMSException {
        if (closed) {
            throw JMSErrorEnum.SESSION_CLOSED.exception();
        }
        return true;
    }


    @Override
    public void recover() throws JMSException {

    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return null;
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {

    }

    /**
     * 删除这个主题的监听,如果发现这个主题已经没有监听器了,会通知connection 我不监听这个主题了
     * @param topicName
     * @param listener
     */
    public void removeTopicListener(String topicName,MessageListener listener){
        if (StringUtils.isBlank(topicName) || listener == null){
            throw new NullPointerException();
        }
        List<MessageListener> listeners = topicListeners.get(topicName);
        if (listeners != null){
            listeners.remove(listener);
        }
        if (listeners.isEmpty()){
            //通知connection 我不订阅这个主题了
            ctx.connection().cancelSubscribe(topicName,this);
        }
    }

    public void topicListenerChange(String topicName,MessageListener oldListener,MessageListener newListener){
        if (StringUtils.isBlank(topicName) || newListener == null){
            throw new NullPointerException();
        }
        this.addTopicListener(topicName,newListener);
        if (oldListener == null){
            return;
        }
        this.removeTopicListener(topicName,oldListener);
    }

    public void queueListenerChange(String queueName,MessageListener oldListener,MessageListener newListener){
        if (StringUtils.isBlank(queueName) || newListener == null){
            throw new NullPointerException();
        }
        this.addQueueListener(queueName,newListener);
        if (oldListener == null){
            return;
        }
        this.removeQueueListener(queueName,oldListener);
    }

    /**
     * 删除队列的监听器,如果发现已经没有监听了,会通知connection 我不订阅这个主题了
     * @param queueName
     * @param listener
     */
    public void removeQueueListener(String queueName,MessageListener listener){
        if (StringUtils.isBlank(queueName) || listener == null){
            throw new NullPointerException();
        }
        BlockingDeque<MessageListener> listeners = queueListeners.get(queueName);
        if (listeners != null){
            listeners.remove(listener);
        }
        if (listeners.isEmpty()){
            //通知connection 我不监听这个队列了
            ctx.connection().cancelListen(queueName,this);
        }
    }

    public void addTopicListener(String topicName,MessageListener listener){
        if (StringUtils.isBlank(topicName) || listener == null){
            throw new NullPointerException();
        }
        synchronized (topicListeners){
            List<MessageListener> listeners = topicListeners.get(topicName);
            if (listeners == null){
                listeners = new CopyOnWriteArrayList<>();
                topicListeners.put(topicName,listeners);
                ctx.connection().subscribe(topicName,this);
            }
            listeners.add(listener);
        }

    }

    public void addQueueListener(String queueName,MessageListener listener){
        if (StringUtils.isBlank(queueName) || listener == null){
            throw new NullPointerException();
        }
        synchronized (queueListeners){
            BlockingDeque<MessageListener> listeners = queueListeners.get(queueName);
            if (listeners == null){
                listeners = new LinkedBlockingDeque<>();
                queueListeners.put(queueName,listeners);
                ctx.connection().listen(queueName,this);
            }
            listeners.add(listener);
        }

    }

    /**
     * session thread
     */
    @Override
    public void run() {
        for (;;) {
            try {
                SessionTask task = tasks.take();
                int type = task.getType();
                Queue queue = task.getQueue();
                Topic topic = task.getTopic();
                TextMessage msg = (TextMessage) task.getMessage();
                Metadata metadata = null;
                MessageDesc desc = null;
                ProtocolBean bean = null;
                Message message = task.getMessage();
                switch (type) {
                    case SessionTask.PUBLISH:
                        //发布主题


                        break;
                    case SessionTask.SESSION_CLOSE:
                        //session 关闭
                        closeAndCleanSession();
                        return;

                    case SessionTask.QUEUE_SEND:
                        //发送队列消息
                        metadata = new Metadata();
                        desc = new MessageDesc();
                        bean = new ProtocolBean();
                        desc.setType(CommonConstant.MessageType.QUEUE);
                        desc.setTitle(queue.getQueueName());
                        bean.setZkName(ctx.connection().myName());
                        bean.setDataBytes(msg.getText().getBytes(Charset.forName("utf-8")));
                        bean.setCommand(CommonConstant.Command.SEND);
                        List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, desc, bean);
                        ctx.connection().write(bytes);
                        break;
                    default:
                        break;
                }
            } catch (InterruptedException e) {
                //session 关闭
                try {
                    close();
                } catch (JMSException je) {
                    //关闭session报错,强行关闭
                    log.error("close session error");
                    closeAndCleanSession();
                }
                log.info("session closed");
                break;
            } catch (JMSException e) {
                log.error("execute task error",e);
            }
        }
    }


    public boolean handleQueueMessage(String queueName,String text){
        try {
            checkClose();
        } catch (JMSException e) {
            return false;
        }
        BlockingDeque<MessageListener> listeners = queueListeners.get(queueName);
        if (listeners == null || listeners.isEmpty()){
            return false;
        }
        MessageListener l = listeners.poll();
        if (l != null){
            l.onMessage(new YxzDefaultTextMessage(text));
            listeners.add(l);
            return true;
        }
        return false;
    }


    /**
     * 清理session信息
     */
    private void closeAndCleanSession() {
        lock.lock();
        try {
            closed = true;
            ctx.connection().cleanAndRemoveSession(this);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 添加session任务,session关键操作,需要识别关闭标识
     *
     * @param task
     */
    public void addTask(SessionTask task) throws JMSException {
        lock.lock();
        try {
            checkClose();
            tasks.add(task);
        } finally {
            lock.unlock();
        }
    }


    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        checkClose();
        if (destination == null) {
            throw new NullPointerException("destination can not be null");
        }
        /**
         * 主题模式的地址
         */
        if (destination instanceof Topic) {
            YxzNettyTopicPublisher publisher = new YxzNettyTopicPublisher((Topic) destination, ctx.createCtx(this));
            return publisher;
        }
        YxzNettyQueueSender sender = new YxzNettyQueueSender((Queue) destination, ctx.createCtx(this));
        return sender;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        checkClose();
        if (destination == null){
            throw new NullPointerException("destination can not be null");
        }
        /**
         * 主题模式的地址
         */
        if (destination instanceof Topic){
           YxzNettyTopicSubscriber subscriber = new YxzNettyTopicSubscriber((Topic)destination,ctx.createCtx(this));
           return subscriber;
        }
        //队列模式
        YxzNettyQueueConsumer consumer = new YxzNettyQueueConsumer((Queue) destination,ctx.createCtx(this));
        return consumer;
    }

    @Override
    public Queue createQueue(String queueName) throws JMSException {
        checkClose();
        if (StringUtils.isBlank(queueName)){
            throw new NullPointerException();
        }
        return new YxzQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        checkClose();
        if (StringUtils.isBlank(topicName)){
            throw new NullPointerException();
        }
        return new YxzTopic(topicName);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        return null;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
