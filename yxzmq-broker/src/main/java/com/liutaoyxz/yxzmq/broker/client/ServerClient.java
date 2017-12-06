package com.liutaoyxz.yxzmq.broker.client;

import java.io.Closeable;
import java.util.List;

/**
 * @author Doug Tao
 * @Date: 10:15 2017/12/5
 * @Description:
 */
public interface ServerClient extends Closeable {


    /**
     * 写数据
     * @param bytes 数据的字节数组
     * @param sync 是否异步,true 同步,false 异步
     * @return 成功或者失败
     */
    boolean write(List<byte[]> bytes,boolean sync) throws InterruptedException;

    /**
     * 读数据
     * @param bytes 读取到的数组
     * @param sync 是否异步,true 同步,false 异步
     */
    void read(List<byte[]> bytes,boolean sync);

    void read(byte[] bytes);

    /**
     * 获得client的name,这个name唯一,可以根据name映射到唯一的client
     * @return
     */
    String name();

    /**
     * 一个channel的唯一标识
     * @return
     */
    String id();

    /**
     * 设置client name
     * @param name
     */
    void setName(String name);

    /**
     * 这个连接是否是broker
     * @return
     */
    boolean isBroker();

    /**
     * 设置是否是broker
     * @param isBroker
     */
    void setIsBroker(boolean isBroker);

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

    /**
     * 准备好了
     */
    void ready();

//    /**
//     * 标记为不可用
//     */
//    void unavailable();

}
