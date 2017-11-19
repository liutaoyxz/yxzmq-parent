package com.liutaoyxz.yxzmq.client.session;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date 上午11:47 2017/11/19
 * @Description:
 */
public class YxzDefaultTopicPublisher extends AbstractTopicPublisher {




    private Topic topic;

    private YxzDefaultSession session;

    YxzDefaultTopicPublisher(Topic topic,YxzDefaultSession session){
        this.session = session;
        this.topic = topic;
    }

    /**
     * 获得主题
     * @return
     * @throws JMSException
     */
    @Override
    public Topic getTopic() throws JMSException {
        return this.topic;
    }

    /**
     * 向固定主题发布消息
     * @param topic
     * @param message
     * @throws JMSException
     */
    @Override
    public void publish(Topic topic, Message message) throws JMSException {

    }

    /**
     * 暂时也不用,使用固定主题的
     * @param destination
     * @param message
     * @throws JMSException
     */
    @Override
    public void send(Destination destination, Message message) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(Message message) throws JMSException {
        YxzSessionTask task = new YxzSessionTask(this.session,YxzSessionTask.PUBLISH);
        task.setMessage((TextMessage) message);
        task.setTopic(this.topic);
        this.session.addTask(task);
    }
}
