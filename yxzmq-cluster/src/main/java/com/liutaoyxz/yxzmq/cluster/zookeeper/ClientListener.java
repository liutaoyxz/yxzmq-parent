package com.liutaoyxz.yxzmq.cluster.zookeeper;

import java.util.List;

/**
 * @author Doug Tao
 * @Date 下午11:43 2017/12/9
 * @Description: client 端的回调
 */
public interface ClientListener {




    List<String> delBrokers(List<String> brokerNames);

    List<String> addBrokers(List<String> brokerNames);

}
