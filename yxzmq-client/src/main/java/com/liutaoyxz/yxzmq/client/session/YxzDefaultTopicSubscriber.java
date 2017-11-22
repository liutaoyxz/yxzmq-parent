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

    private YxzDefaultSession session;



    YxzDefaultTopicSubscriber(Topic topic,YxzDefaultSession session){
        this.topic = topic;
        this.session = session;
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
        if (this.messageListener == null){
            //告诉connection 我要注册监听了,把我放到列表里
            YxzSessionTask task = new YxzSessionTask(this.session,YxzSessionTask.SUBSCRIBE);
            task.setTopic(this.topic);
            this.session.getConnection().addTopicSubscriber(this);
            this.session.addTask(task);
        }
        this.messageListener = messageListener;
    }
}
