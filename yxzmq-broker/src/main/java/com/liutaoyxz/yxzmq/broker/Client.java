package com.liutaoyxz.yxzmq.broker;

import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * @author Doug Tao
 * @Date: 13:54 2017/11/15
 * @Description: 一个client就代表一个连接,client 包装了连接
 *               一个client 是在一次连接之后生成的,连接取消后,client也需要取消
 */
public interface Client {

    /**
     * 这个连接是否正在被read
     * @return
     */
    boolean reading();

    /**
     * 开始读取数据
     * @return true 可以开始读,  false,别人在读
     */
    boolean startRead();

    /**
     * 获取client 的唯一id
     * @return
     */
    String id();

    /**
     * 获取客户端的连接地址
     * @return
     */
    String address();

    /**
     * 获取client与server 连接的channel
     * @return
     */
    SocketChannel channel();

    void stopRead();

    List<ProtocolBean> read(ByteBuffer buffer);

    Group parent();

    ChannelHandler handler();

    void setParent(Group group);

    void setIsMainChannel(boolean isMainChannel);

    Runnable getDataReadTask();

}
