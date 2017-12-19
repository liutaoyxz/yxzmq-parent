package com.liutaoyxz.yxzmq.cluster.zookeeper;

import com.liutaoyxz.yxzmq.cluster.broker.Broker;

import java.util.List;

/**
 * @author Doug Tao
 * @Date 下午11:43 2017/12/9
 * @Description: client 端的回调
 */
public interface ClientListener {


    /**
     * 删除broker
     * @param brokers
     * @return
     */
    List<String> delBrokers(List<Broker> brokers);

    /**
     * 增加了broker
     * @param brokers
     * @return
     */
    List<String> addBrokers(List<Broker> brokers) ;

}
