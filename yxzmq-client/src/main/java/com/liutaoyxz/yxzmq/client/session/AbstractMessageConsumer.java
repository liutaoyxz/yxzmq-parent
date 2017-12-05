package com.liutaoyxz.yxzmq.client.session;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

/**
 * @author Doug Tao
 * @Date 下午4:24 2017/11/19
 * @Description:
 */
public abstract class AbstractMessageConsumer implements MessageConsumer {

    @Override
    public String getMessageSelector() throws JMSException {
        return null;
    }

    @Override
    public Message receive() throws JMSException {
        return null;
    }

    @Override
    public Message receive(long l) throws JMSException {
        return null;
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        return null;
    }

    @Override
    public void close() throws JMSException {

    }
}
