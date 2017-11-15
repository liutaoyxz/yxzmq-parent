package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.Selector;

/**
 * @author Doug Tao
 * @Date: 9:15 2017/11/15
 * @Description:
 */
public abstract class AbstractServer implements Server {

    protected Logger log = null;

    protected static final String DEFAULT_CHARSET = "utf-8";

    protected ServerConfig config;

    protected ChannelHandler channelHandler;

    protected AbstractServer(Logger log,ChannelHandler channelHandler) {
        this.log = log;
        this.channelHandler = channelHandler;
    }

    /**
     * server 启动
     */
    @Override
    public abstract void start();

    /**
     * server  终止
     */
    @Override
    public abstract void stop();

    @Override
    public Server setConfig(ServerConfig config) {
        if (config == null) {
            throw new NullPointerException("config can not be null");
        }
        return this;
    }


}