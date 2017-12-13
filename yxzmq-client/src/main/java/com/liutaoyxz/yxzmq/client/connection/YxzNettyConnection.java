package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * @author Doug Tao
 * @Date 下午7:37 2017/12/10
 * @Description:
 */
public class YxzNettyConnection extends AbstractConnection {

    private static final Logger log = LoggerFactory.getLogger(YxzNettyConnection.class);




    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return null;
    }

    /**
     * clientID , 全局唯一
     * @param clientID 采用zookeeper 顺序分配,不会重复
     * @throws JMSException 不支持自己配置
     */
    @Override
    public void setClientID(String clientID) throws JMSException {
        throw JMSErrorEnum.OP_NOT_SUPPORT.exception();
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
