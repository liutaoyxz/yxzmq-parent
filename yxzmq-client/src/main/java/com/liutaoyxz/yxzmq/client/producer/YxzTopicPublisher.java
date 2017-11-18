package com.liutaoyxz.yxzmq.client.producer;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date 下午9:09 2017/11/18
 * @Description:
 */
public class YxzTopicPublisher extends AbstractProducer implements TopicPublisher {

    /**
     * 获得主题
     * @return
     * @throws JMSException
     */
    @Override
    public Topic getTopic() throws JMSException {
        return null;
    }

    /**
     * 发布,向所有人发布
     * @param message
     * @throws JMSException
     */
    @Override
    public void publish(Message message) throws JMSException {

    }

    /**
     * 暂时不知道啥意思,应该是优先级和过期时间
     * @param message
     * @param i
     * @param i1
     * @param l
     * @throws JMSException
     */
    @Override
    public void publish(Message message, int i, int i1, long l) throws JMSException {

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
     * 暂时不用
     * @param topic
     * @param message
     * @param i
     * @param i1
     * @param l
     * @throws JMSException
     */
    @Override
    public void publish(Topic topic, Message message, int i, int i1, long l) throws JMSException {

    }

    /**
     * 暂时也不用,使用固定主题的
     * @param destination
     * @param message
     * @throws JMSException
     */
    @Override
    public void send(Destination destination, Message message) throws JMSException {

    }

    /**
     * 暂时不用
     * @param destination
     * @param message
     * @param completionListener
     * @throws JMSException
     */
    @Override
    public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {

    }
}
