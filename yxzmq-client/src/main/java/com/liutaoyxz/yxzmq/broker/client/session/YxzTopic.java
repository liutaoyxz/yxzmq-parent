package com.liutaoyxz.yxzmq.broker.client.session;

import javax.jms.JMSException;
import javax.jms.Topic;

/**
 * @author Doug Tao
 * @Date 上午11:08 2017/11/19
 * @Description:
 */
public class YxzTopic implements Topic {
    private String topicName;

    YxzTopic(String topicName){
        this.topicName = topicName;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.topicName;
    }

    @Override
    public String toString() {
        return this.topicName;
    }
}
