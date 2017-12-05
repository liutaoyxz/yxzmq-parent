package com.liutaoyxz.yxzmq.broker;

import com.liutaoyxz.yxzmq.broker.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server 主类,放弃netty, 采用jdk 自带的nio
 */
public class YxzmqMain {

    private static final int DEFAULT_PORT = 11171;

    private static final Logger LOGGER = LoggerFactory.getLogger(YxzmqMain.class);

    public static void main(String[] args) throws InterruptedException {
//        DefaultChannelHandler handler = new DefaultChannelHandler();
//        Server server = new DefaultServer(handler);
//        server.start();
//        LOGGER.info("start .........");
        NettyServer server = NettyServer.getServer();
        server.start();


    }

}
