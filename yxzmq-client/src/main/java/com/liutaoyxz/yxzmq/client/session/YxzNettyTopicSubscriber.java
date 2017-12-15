package com.liutaoyxz.yxzmq.client.session;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

/**
 * @author Doug Tao
 * @Date 下午4:20 2017/11/19
 * @Description:
 */
public class YxzNettyTopicSubscriber extends AbstractMessageConsumer implements TopicSubscriber{

    private Topic topic;

    private MessageListener messageListener;


    public YxzNettyTopicSubscriber(Topic topic, MessageListener messageListener) {
        this.topic = topic;
        this.messageListener = messageListener;
    }

    @Override
    public Topic getTopic() throws JMSException {
        return this.topic;
    }

    @Override
    public boolean getNoLocal() throws JMSException {
        return false;
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return this.messageListener;
    }

    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {

    }
}
