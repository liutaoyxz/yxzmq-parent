package com.liutaoyxz.yxzmq.client.connection;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Doug Tao
 * @Date 下午7:37 2017/12/10
 * @Description:
 */
public class YxzNettyConnection extends AbstractConnection {

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return null;
    }

    @Override
    public void setClientID(String clientID) throws JMSException {

    }

    @Override
    public void start() throws JMSException {

    }

    @Override
    public void stop() throws JMSException {

    }

    @Override
    public void close() throws JMSException {

    }
}
