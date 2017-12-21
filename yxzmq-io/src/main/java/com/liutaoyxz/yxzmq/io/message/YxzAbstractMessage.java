package com.liutaoyxz.yxzmq.io.message;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Doug Tao
 * @Date: 9:34 2017/12/21
 * @Description: 实现message 接口公用部分
 * 所有子类都是按照jms规范实现,每个子类需要有一个schema
 *
 *
 */
public abstract class YxzAbstractMessage implements Message{

    public static final String MESSAGE_ID_PREFIX = "ID:";

    /** 消息id,保证全局唯一,采用zkName+client端自增标识 **/
    private String messageID;

    /** 消息发送时的时间,不是创建的时间 **/
    private Long timestamp;

    /** 发送地址 **/
    private Destination destination;

    /** 回复地址 **/
    private Destination replyDestination;

    /** 过期时间 **/
    private Long expiration;


    @Override
    public String getJMSMessageID() throws JMSException {
        return this.messageID;
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        this.messageID = id;
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        return this.timestamp == null?0:timestamp;
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        this.timestamp = timestamp;
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        return this.replyDestination;
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        this.replyDestination = replyTo;
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return this.destination;
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        this.destination = destination;
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {

    }

    @Override
    public String getJMSType() throws JMSException {
        return null;
    }

    @Override
    public void setJMSType(String type) throws JMSException {

    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return 0;
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {

    }
}
