package com.liutaoyxz.yxzmq.cluster.broker;

import org.apache.zookeeper.KeeperException;

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
    boolean checkRoot() throws KeeperException, InterruptedException, Exception;


    /**
     * 注册到zookeeper
     * @return
     */
    boolean register();

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



    /**
     * 启动
     * @throws Exception 抛出异常,启动时停止
     */
    void start() throws Exception;




}
