package com.liutaoyxz.yxzmq.client.session;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date 下午4:20 2017/11/19
 * @Description:
 */
public class YxzDefaultTopicSubscriber extends AbstractMessageConsumer implements TopicSubscriber{

    private Topic topic;

    private MessageListener messageListener;

    YxzDefaultTopicSubscriber(Topic topic){
        this.topic = topic;
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
        this.messageListener = messageListener;
    }
}
