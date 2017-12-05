package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;

/**
 * broker server
 */
public interface Server {

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 设置server config
     * @param config
     * @return
     */
    Server setConfig(ServerConfig config);

    /**
     * 连接到目标地址
     * @param host
     * @param port
     */
    void connect(String host,int port) throws InterruptedException;
}
