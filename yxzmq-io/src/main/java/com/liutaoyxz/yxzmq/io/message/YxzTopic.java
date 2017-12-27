package com.liutaoyxz.yxzmq.io.message;

import javax.jms.JMSException;
import javax.jms.Topic;

/**
 * @author Doug Tao
 * @Date: 16:28 2017/12/27
 * @Description:
 */
public class YxzTopic extends AbstractDestination implements Topic {

    private String topicName;

    public YxzTopic() {
    }

    public YxzTopic(String topicName, DestinationId destinationId) {
        this(destinationId,YxzTopic.class,topicName);
    }

    private YxzTopic(DestinationId destinationId, Class<? extends AbstractDestination> destinationClass, String topicName) {
        super(destinationId, destinationClass);
        this.topicName = topicName;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.topicName;
    }
}
