package com.liutaoyxz.yxzmq.client.session;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 16:00 2017/12/13
 * @Description:
 */
public class YxzNettySession extends AbstractSession {


    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        return null;
    }

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
    public void close() throws JMSException {

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

    @Override
    public void run() {

    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        return null;
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
