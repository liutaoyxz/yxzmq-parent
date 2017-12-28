package com.liutaoyxz.yxzmq.io.message;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Doug Tao
 * @Date: 17:28 2017/12/28
 * @Description:
 */
public class YxzQueue extends AbstractDestination implements Queue {

    private String queueName;

    public YxzQueue() {
    }

    public YxzQueue(String queueName) {
        super(AbstractDestination.YXZ_QUEUE_CLASS);
        this.queueName = queueName;
    }

    public YxzQueue(String queueName,DestinationId destinationId) {
        this(queueName,destinationId,AbstractDestination.YXZ_QUEUE_CLASS);
    }

    private YxzQueue(String queueName,DestinationId destinationId,String destinationClass){
        super(destinationId, destinationClass);
        this.queueName = queueName;
    }

    @Override
    public String getQueueName() throws JMSException {
        return null;
    }
}
