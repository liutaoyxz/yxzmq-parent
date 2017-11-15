package com.liutaoyxz.yxzmq.client;

import com.liutaoyxz.yxzmq.io.datahandler.ChannelReader;

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
     * @param reader 读取数据的reader
     */
    void startRead(ChannelReader reader);

}
