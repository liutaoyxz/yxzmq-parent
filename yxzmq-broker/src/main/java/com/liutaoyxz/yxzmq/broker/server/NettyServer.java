package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;

/**
 * @author Doug Tao
 * @Date 下午10:21 2017/12/4
 * @Description: 转用netty实现broker 和client
 */
public class NettyServer implements Server {

    private ServerConfig config;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Server setConfig(ServerConfig config) {
        return null;
    }
}
