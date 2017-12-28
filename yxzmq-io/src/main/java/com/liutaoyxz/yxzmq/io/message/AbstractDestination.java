package com.liutaoyxz.yxzmq.io.message;

import javax.jms.Destination;

/**
 * @author Doug Tao
 * @Date: 15:20 2017/12/27
 * @Description:
 */
public class AbstractDestination implements Destination {

    public static final String YXZ_TOPIC_CLASS = "yxzTopic";

    public static final String YXZ_QUEUE_CLASS = "yxzQueue";

    private DestinationId destinationId;

    private String destinationClass;

    public AbstractDestination() {
    }

    protected AbstractDestination(String destinationClass){
        this.destinationClass = destinationClass;
    }

    public AbstractDestination(DestinationId destinationId,String destinationClass) {
        this.destinationId = destinationId;
        this.destinationClass = destinationClass;
    }

    public String getDestinationClass() {
        return destinationClass;
    }

    public void setDestinationClass(String destinationClass) {
        this.destinationClass = destinationClass;
    }

    public DestinationId getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(DestinationId destinationId) {
        this.destinationId = destinationId;
    }
}
