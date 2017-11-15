package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.datahandler.ChannelReader;
import org.slf4j.Logger;

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

    protected ChannelReader reader;

    protected AbstractServer(Logger log,ChannelHandler channelHandler,ChannelReader reader) {
        this.log = log;
        this.channelHandler = channelHandler;
        this.reader = reader;
    }


    @Override
    public Server setConfig(ServerConfig config) {
        if (config == null) {
            throw new NullPointerException("config can not be null");
        }
        return this;
    }


}
