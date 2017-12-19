package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ClientListener;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 15:19 2017/12/14
 * @Description:
 */
public class ZkClientListener implements ClientListener {

    private YxzNettyConnection connection;

    public ZkClientListener(){

    }

    public YxzNettyConnection getConnection() {
        return connection;
    }

    public void setConnection(YxzNettyConnection connection) {
        this.connection = connection;
    }

    @Override
    public List<String> delBrokers(List<Broker> brokers) {
        return connection.delBrokers(brokers);
    }

    @Override
    public List<String> addBrokers(List<Broker> brokers) {
        return connection.addBrokers(brokers);
    }
}
