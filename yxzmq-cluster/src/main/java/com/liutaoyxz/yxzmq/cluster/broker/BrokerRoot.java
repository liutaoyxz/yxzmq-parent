package com.liutaoyxz.yxzmq.cluster.broker;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 15:36 2017/11/27
 * @Description: 根目录操作,/yxzmq
 */
public interface BrokerRoot {


    /**
     * 检查根目录是否存在并且监视根目录
     * @return
     */
    boolean checkRoot();


    /**
     * 注册到zookeeper
     * @param port
     * @return
     */
    boolean register(int port);

    /**
     * 获得broker 的列表
     * @return
     */
    List<String> brokers() throws InterruptedException;

    /**
     * 获得本机的broker对象
     * @return
     */
    Broker getBroker();




}
