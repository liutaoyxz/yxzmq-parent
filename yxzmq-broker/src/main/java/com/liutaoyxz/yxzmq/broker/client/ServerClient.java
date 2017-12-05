package com.liutaoyxz.yxzmq.broker.client;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 10:15 2017/12/5
 * @Description:
 */
public interface ServerClient {


    /**
     * 写数据
     * @param bytes 数据的字节数组
     * @param sync 是否异步,false 同步,true 异步
     * @return 成功或者失败
     */
    boolean write(List<byte[]> bytes,boolean sync);

    /**
     * 读数据
     * @param bytes 读取到的数组
     * @param sync 是否异步,false 同步,true 异步
     */
    void read(List<byte[]> bytes,boolean sync);

    /**
     * 获得client的name,这个name唯一,可以根据name映射到唯一的client
     * @return
     */
    String name();

    /**
     * 这个连接是否是broker
     * @return
     */
    boolean isBroker();

    /**
     * 获取这个连接的远程地址
     * @return
     */
    String remoteAddress();

    /**
     * 获取这个连接的远程端口
     * @return
     */
    int remotePort();

    /**
     * 连接是否可用
     * @return
     */
    boolean available();

//    /**
//     * 准备好了
//     */
//    void ready();
//
//    /**
//     * 标记为不可用
//     */
//    void unavailable();

}
