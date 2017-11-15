package com.liutaoyxz.yxzmq.client;

import com.liutaoyxz.yxzmq.io.protocol.Message;

/**
 * @author Doug Tao
 * @Date: 14:05 2017/11/15
 * @Description: 接收数据的监听器,当读取到完整的数据时,需要触发 receiveMessage 方法
 */
public interface ReaderListener {


    /**
     * 接收取到服务器发来的消息,触发此监听器进行处理
     * @param message
     */
    void receiveMessage(Message message);

}
