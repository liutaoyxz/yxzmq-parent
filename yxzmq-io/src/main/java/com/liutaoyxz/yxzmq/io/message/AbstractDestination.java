package com.liutaoyxz.yxzmq.io.message;

import javax.jms.Destination;

/**
 * @author Doug Tao
 * @Date: 15:20 2017/12/27
 * @Description:
 */
public class AbstractDestination implements Destination {

    private DestinationId destinationId;

    private Class<? extends AbstractDestination> destinationClass;

    public AbstractDestination() {
    }

    public AbstractDestination(DestinationId destinationId,Class<? extends AbstractDestination> destinationClass) {
        this.destinationId = destinationId;
        this.destinationClass = destinationClass;
    }

    public Class<? extends AbstractDestination> getDestinationClass() {
        return destinationClass;
    }

    public void setDestinationClass(Class<? extends AbstractDestination> destinationClass) {
        this.destinationClass = destinationClass;
    }

    public DestinationId getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(DestinationId destinationId) {
        this.destinationId = destinationId;
    }
}
