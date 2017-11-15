package com.liutaoyxz.yxzmq.io.datahandler;

import com.liutaoyxz.yxzmq.io.protocol.Message;

/**
 * @author Doug Tao
 * @Date: 14:05 2017/11/15
 * @Description: 读取数据的监听器,当读取到完整的数据是,需要触发 readComplete 方法
 */
public interface ReaderListener {


    /**
     * 读取到服务器发来的消息,触发此监听器进行处理
     * @param message
     */
    void receiveMessage(Message message);

}
