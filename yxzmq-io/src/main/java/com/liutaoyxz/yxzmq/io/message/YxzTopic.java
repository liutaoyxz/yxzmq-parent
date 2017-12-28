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

    public YxzTopic(String topicName) {
        super(AbstractDestination.YXZ_TOPIC_CLASS);
        this.topicName = topicName;
    }

    public YxzTopic(String topicName, DestinationId destinationId) {
        this(destinationId,AbstractDestination.YXZ_TOPIC_CLASS,topicName);
    }

    private YxzTopic(DestinationId destinationId, String destinationClass, String topicName) {
        super(destinationId, destinationClass);
        this.topicName = topicName;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.topicName;
    }
}
