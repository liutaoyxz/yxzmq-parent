package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Doug Tao
 * @Date 下午9:32 2017/11/18
 * @Description: 采用单例模式
 * 一个连接对应一个或者多个channel,一个session只能对应一个channel.
 * 连接在start()之后与broker 开始连接,保存到ChannelContainer中
 *
 */
@JMSConnectionFactory("yxz")
public class YxzDefaultConnectionFactory implements ConnectionFactory{


    /**
     * 默认ip
     */
    private static final String DEFAULT_ADDRESS = "127.0.0.1:11171";

    private static final YxzDefaultConnectionFactory FACTORY = new YxzDefaultConnectionFactory();

    public static final Logger log = LoggerFactory.getLogger(YxzDefaultConnectionFactory.class);

    private YxzDefaultConnectionFactory(){

    }

    public static YxzDefaultConnectionFactory getFactory(){
        return FACTORY;
    }

    @Override
    public Connection createConnection() throws JMSException {
        return createConnection(DEFAULT_ADDRESS,null);
    }

    /**
     * 创建一个connection
     * @param address 地址  ip
     * @param str 暂时不用
     * @return
     * @throws JMSException
     */
    @Override
    public Connection createConnection(String address, String str) throws JMSException {
        YxzDefaultConnection connection = new YxzDefaultConnection(1, createAddress(address));
        try {
            connection.setClientID(ConnectionContainer.createClientID());
            connection.init();
        } catch (IOException e) {
            log.debug("socketChannel open error",e);
            JMSException exception = JMSErrorEnum.CHANNEL_OPEN_ERROR.exception();
            exception.setLinkedException(e);
            throw exception;
        }
        return connection;
    }

    private InetSocketAddress createAddress(String address){
        String[] ss = address.split(":");
        String ip = ss[0];
        int port = Integer.valueOf(ss[1]);
        return new InetSocketAddress(ip,port);
    }

    @Override
    public JMSContext createContext() {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1) {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1, int i) {
        return null;
    }

    @Override
    public JMSContext createContext(int i) {
        return null;
    }
}
