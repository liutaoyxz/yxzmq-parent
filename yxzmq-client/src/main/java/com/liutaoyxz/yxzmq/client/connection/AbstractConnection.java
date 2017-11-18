package com.liutaoyxz.yxzmq.client.connection;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date 下午10:12 2017/11/18
 * @Description:
 */
public abstract class AbstractConnection implements Connection {

    protected String clientID;


    @Override
    public Session createSession(int i) throws JMSException {
        return createSession(true,i);
    }

    @Override
    public Session createSession() throws JMSException {
        return createSession(true,0);
    }

    @Override
    public String getClientID() throws JMSException {
        return this.clientID;
    }


    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return null;
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener exceptionListener) throws JMSException {

    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String s, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String s, String s1, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String s, String s1, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String s, String s1, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }
}
