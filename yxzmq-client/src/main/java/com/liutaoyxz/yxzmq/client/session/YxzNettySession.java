package com.liutaoyxz.yxzmq.client.session;

import com.liutaoyxz.yxzmq.client.YxzClientContext;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 16:00 2017/12/13
 * @Description:
 */
public class YxzNettySession extends AbstractSession {

    private static final Logger log = LoggerFactory.getLogger(YxzNettySession.class);

    /** 上下文对象 **/
    private YxzClientContext ctx;

    private boolean transacted;

    private int acknowledgeMode;

    private ReentrantLock lock = new ReentrantLock();

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
        return null;
    }

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

    @Override
    public void close() throws JMSException {
        lock.lock();
        try {
            ctx.setSession(this);
            SessionTask closeTask = new SessionTask(SessionTask.SESSION_CLOSE,ctx);
            addTask(closeTask);
            this.closed = true;
        }finally {
            lock.unlock();
        }
    }

    /**
     * 检查session 是否关闭,必须加锁,需要与发布任务还有关闭session 操作互斥
     * @return
     * @throws JMSException
     */
    private boolean checkClose() throws JMSException {
        if (closed){
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
     * session thread
     */
    @Override
    public void run() {
        for (;;){
            try {
                SessionTask task = tasks.take();
                int type = task.getType();
                Queue queue = null;
                Topic topic = null;
                Message message = task.getMessage();
                switch (type){
                    case SessionTask.PUBLISH:
                        //发布主题



                        break;
                    case SessionTask.SESSION_CLOSE:
                        //session 关闭
                        cleanSession();
                        return;
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
                    cleanSession();
                }
                log.info("session closed");
                break;
            }
        }
    }

    /**
     * 清理session信息
     */
    private void cleanSession(){

    }

    /**
     * 添加session任务,session关键操作,需要识别关闭标识
     * @param task
     */
    public void addTask(SessionTask task) throws JMSException {
        lock.lock();
        try {
            checkClose();
            tasks.add(task);
        }finally {
            lock.unlock();
        }


    }


    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        checkClose();
        if (destination == null){
            throw new NullPointerException("destination can not be null");
        }
        ctx.setSession(this);
        /**
         * 主题模式的地址
         */
        if (destination instanceof Topic){
            YxzNettyTopicPublisher publisher = new YxzNettyTopicPublisher((Topic) destination,ctx);
            return publisher;
        }
        YxzNettyQueueSender sender = new YxzNettyQueueSender((Queue)destination,ctx);
        return sender;
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        return null;
    }

    @Override
    public Queue createQueue(String queueName) throws JMSException {
        return null;
    }

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        return null;
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        return null;
    }
}
