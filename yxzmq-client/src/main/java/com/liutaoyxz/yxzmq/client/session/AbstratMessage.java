package com.liutaoyxz.yxzmq.client.session;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Enumeration;

/**
 * @author Doug Tao
 * @Date 下午7:09 2017/11/18
 * @Description:
 */
public abstract class AbstratMessage implements Message {

    private String id;

    private int priority = Message.DEFAULT_PRIORITY;


    /**
     * 时间戳
     */
    private long jmsTimestamp;

    @Override
    public String getJMSMessageID() throws JMSException {
        return this.id;
    }

    @Override
    public void setJMSMessageID(String s) throws JMSException {
        this.id = id;
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return this.jmsTimestamp;
    }

    @Override
    public void setJMSTimestamp(long jmsTimestamp) throws JMSException {
        this.jmsTimestamp = jmsTimestamp;
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        throw new JMSException("not support");
//        return new byte[0];
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {
        throw new JMSException("not support");
    }

    @Override
    public void setJMSCorrelationID(String s) throws JMSException {
        throw new JMSException("not support");
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
//        return null;
        throw new JMSException("not support");
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
//        return null;
        throw new JMSException("not support");
    }

    @Override
    public void setJMSReplyTo(Destination destination) throws JMSException {
        throw new JMSException("not support");
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return null;
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {

    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSDeliveryMode(int i) throws JMSException {

    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        return false;
    }

    @Override
    public void setJMSRedelivered(boolean b) throws JMSException {

    }

    @Override
    public String getJMSType() throws JMSException {
        return null;
    }

    @Override
    public void setJMSType(String s) throws JMSException {

    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSExpiration(long l) throws JMSException {

    }

    @Override
    public long getJMSDeliveryTime() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSDeliveryTime(long l) throws JMSException {

    }

    @Override
    public int getJMSPriority() throws JMSException {
        return this.priority;
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        this.priority = priority;
    }

    @Override
    public void clearProperties() throws JMSException {

    }

    @Override
    public boolean propertyExists(String s) throws JMSException {
        return false;
    }

    @Override
    public boolean getBooleanProperty(String s) throws JMSException {
        return false;
    }

    @Override
    public byte getByteProperty(String s) throws JMSException {
        return 0;
    }

    @Override
    public short getShortProperty(String s) throws JMSException {
        return 0;
    }

    @Override
    public int getIntProperty(String s) throws JMSException {
        return 0;
    }

    @Override
    public long getLongProperty(String s) throws JMSException {
        return 0;
    }

    @Override
    public float getFloatProperty(String s) throws JMSException {
        return 0;
    }

    @Override
    public double getDoubleProperty(String s) throws JMSException {
        return 0;
    }

    @Override
    public String getStringProperty(String s) throws JMSException {
        return null;
    }

    @Override
    public Object getObjectProperty(String s) throws JMSException {
        return null;
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        return null;
    }

    @Override
    public void setBooleanProperty(String s, boolean b) throws JMSException {

    }

    @Override
    public void setByteProperty(String s, byte b) throws JMSException {

    }

    @Override
    public void setShortProperty(String s, short i) throws JMSException {

    }

    @Override
    public void setIntProperty(String s, int i) throws JMSException {

    }

    @Override
    public void setLongProperty(String s, long l) throws JMSException {

    }

    @Override
    public void setFloatProperty(String s, float v) throws JMSException {

    }

    @Override
    public void setDoubleProperty(String s, double v) throws JMSException {

    }

    @Override
    public void setStringProperty(String s, String s1) throws JMSException {

    }

    @Override
    public void setObjectProperty(String s, Object o) throws JMSException {

    }

    @Override
    public void acknowledge() throws JMSException {

    }

    @Override
    public boolean isBodyAssignableTo(Class aClass) throws JMSException {
        return false;
    }
}
