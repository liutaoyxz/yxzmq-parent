package com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Tao
 * @Date: 15:24 2017/11/29
 * @Description:
 */
public class ClientWatcher implements Watcher {

    public static final Logger log = LoggerFactory.getLogger(ClientWatcher.class);

    @Override
    public void process(WatchedEvent event) {
        log.debug("client watch,event is {}",event);
    }
}