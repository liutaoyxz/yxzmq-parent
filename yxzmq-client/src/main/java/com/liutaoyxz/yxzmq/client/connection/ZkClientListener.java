package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ClientListener;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 15:19 2017/12/14
 * @Description:
 */
public class ZkClientListener implements ClientListener {

    @Override
    public List<String> delBrokers(List<String> brokerNames) {
        return null;
    }

    @Override
    public List<String> addBrokers(List<String> brokerNames) {
        return null;
    }
}
