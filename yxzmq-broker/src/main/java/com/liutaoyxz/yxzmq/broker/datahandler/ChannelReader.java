package com.liutaoyxz.yxzmq.broker.datahandler;

import com.liutaoyxz.yxzmq.broker.Client;

/**
 * @author Doug Tao
 * @Date: 14:01 2017/11/15
 * @Description: 读取channel中的数据
 */
public interface ChannelReader {

    /**
     * 开始读取数据
     * @param client
     */
    void startRead(Client client);

}
