package com.liutaoyxz.yxzmq.cluster.zookeeper.watch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Tao
 * @Date: 14:18 2017/11/28
 * @Description:
 */
public class BrokerWatch implements Watcher {

    public static final Logger log = LoggerFactory.getLogger(BrokerWatch.class);

    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        log.debug("watch event,path is {},event is {}",path,event);
        Event.EventType type = event.getType();
        switch (type){
            case None:

                break;
            case NodeCreated:
                break;

            case NodeDeleted:

                break;
            case NodeChildrenChanged:
                break;

            default:
                log.error("event type error,type is {}",type);
                break;
        }
    }
}
