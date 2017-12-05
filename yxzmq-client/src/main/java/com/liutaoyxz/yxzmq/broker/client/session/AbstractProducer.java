package com.liutaoyxz.yxzmq.broker.client.session;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date 下午8:54 2017/11/18
 * @Description:
 */
public abstract class AbstractProducer implements MessageProducer {

    private int priority;

    @Override
    public void setDisableMessageID(boolean b) throws JMSException {

    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        return false;
    }

    @Override
    public void setDisableMessageTimestamp(boolean b) throws JMSException {

    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return false;
    }

    @Override
    public void setDeliveryMode(int i) throws JMSException {

    }

    @Override
    public int getDeliveryMode() throws JMSException {
        return 0;
    }

    @Override
    public void setPriority(int priority) throws JMSException {
        this.priority = priority;
    }

    @Override
    public int getPriority() throws JMSException {
        return  this.priority;
    }

    @Override
    public void setTimeToLive(long l) throws JMSException {

    }

    @Override
    public long getTimeToLive() throws JMSException {
        return 0;
    }

    @Override
    public void setDeliveryDelay(long l) throws JMSException {

    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        return 0;
    }

    @Override
    public Destination getDestination() throws JMSException {
        return null;
    }

    @Override
    public void close() throws JMSException {

    }

    @Override
    public void send(Message message, int i, int i1, long l) throws JMSException {

    }


    @Override
    public void send(Destination destination, Message message, int i, int i1, long l) throws JMSException {

    }

    @Override
    public void send(Message message, CompletionListener completionListener) throws JMSException {

    }

    @Override
    public void send(Message message, int i, int i1, long l, CompletionListener completionListener) throws JMSException {

    }


    @Override
    public void send(Destination destination, Message message, int i, int i1, long l, CompletionListener completionListener) throws JMSException {

    }
}
