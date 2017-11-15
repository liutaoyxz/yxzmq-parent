package com.liutaoyxz.yxzmq.broker.channelhandler;

import com.liutaoyxz.yxzmq.broker.YxzClient;

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

    /**
     * 获得用户client对象
     * @param channel
     * @return 如果用户还没有添加到客户端列表 则返回null, 否则返回实例
     */
    public YxzClient client(SocketChannel channel);

}
