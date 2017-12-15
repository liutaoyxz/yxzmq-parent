package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;
import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.common.Address;

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
    ServerClient connect(String host, int port) throws InterruptedException;

    /**
     * 连接到目标地址
     * @param host
     * @param port
     */
    ServerClient connect(Address address) throws InterruptedException;
}
