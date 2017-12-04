package com.liutaoyxz.yxzmq.cluster.zookeeper.callback;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

/**
 * @author Doug Tao
 * @Date: 14:39 2017/12/1
 * @Description:
 */
public class BrokerChildrenWatcher implements AsyncCallback.DataCallback,Watcher {

    private String brokerName;

    public BrokerChildrenWatcher(String brokerName) {
        this.brokerName = brokerName;
    }

    @Override
    public void process(WatchedEvent event) {

    }

    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

    }
}
