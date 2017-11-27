package com.liutaoyxz.yxzmq.cluster.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Tao
 * @Date: 15:39 2017/11/27
 * @Description:
 */
public class BrokerRoot implements Root {

    private static final Logger log = LoggerFactory.getLogger(BrokerRoot.class);

    @Override
    public boolean checkRoot() {
        return false;
    }



    class RootWatcher implements Watcher{
        @Override
        public void process(WatchedEvent event) {
            String path = event.getPath();
            log.debug("path is {}",path);
            log.debug("watch event :{}",event);
        }
    }


}
