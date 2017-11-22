package com.liutaoyxz.yxzmq.client.session;

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

    YxzQueueConsumer(Queue queue){
        this.queue = queue;
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
