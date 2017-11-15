package com.liutaoyxz.yxzmq.broker.channelhandler;

import java.nio.channels.SocketChannel;


/**
 * @author Doug Tao
 * @Date: 9:15 2017/11/15
 * @Description: 连接处理
 */
public interface ChannelHandler {


    /**
     * 客户端已连接,记录状态
     * @param channel
     * @return
     */
    boolean connect(SocketChannel channel);

    /**
     * 连接取消,状态处理
     * @param channel
     * @return
     */
    boolean disconnect(SocketChannel channel);



}
