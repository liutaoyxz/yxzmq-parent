package com.liutaoyxz.yxzmq.client.session;

import com.liutaoyxz.yxzmq.client.YxzClientContext;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

/**
 * @author Doug Tao
 * @Date: 14:51 2017/12/15
 * @Description:
 */
public class YxzNettyTopicPublisher extends AbstractTopicPublisher  {


    private Topic topic;

    private YxzClientContext ctx;

    public YxzNettyTopicPublisher(Topic topic, YxzClientContext ctx) {
        this.topic = topic;
        this.ctx = ctx;
    }

    @Override
    public Topic getTopic() throws JMSException {
        return topic;
    }

    @Override
    public void publish(Topic topic, Message message) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(Message message) throws JMSException {

    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        throw new UnsupportedOperationException();
    }
}
