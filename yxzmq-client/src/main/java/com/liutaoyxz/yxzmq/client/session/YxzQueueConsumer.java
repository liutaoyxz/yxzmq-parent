package com.liutaoyxz.yxzmq.broker.client.session;

import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date 下午10:37 2017/11/22
 * @Description:
 */
public class YxzQueueConsumer implements QueueReceiver {

    private Queue queue;

    private MessageListener messageListener;

    private YxzDefaultSession session;

    YxzQueueConsumer(Queue queue,YxzDefaultSession session){
        this.queue = queue;
        this.session = session;
    }

    @Override
    public Queue getQueue() throws JMSException {
        return this.queue;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return null;
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return this.messageListener;
    }

    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {
        if (this.messageListener == null){
            //没有注册过监听,告诉broker我要监听
            YxzSessionTask task = new YxzSessionTask(this.session,YxzSessionTask.LISTEN_QUEUE);
            task.setQueue(this.queue);
            this.session.addTask(task);
            this.session.getConnection().addQueueConsumer(this);
        }
        this.messageListener = messageListener;
    }

    @Override
    public Message receive() throws JMSException {
        if (this.queue == null){
            throw JMSErrorEnum.QUEUE_NOT_DEFINE.exception();
        }
        return null;
    }

    @Override
    public Message receive(long l) throws JMSException {
        return null;
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        return null;
    }

    @Override
    public void close() throws JMSException {

    }
}
