package com.liutaoyxz.yxzmq.cluster.zookeeper.watch;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 15:24 2017/11/29
 * @Description:
 */
public class TopicWatch implements Watcher {

    public static final Logger log = LoggerFactory.getLogger(TopicWatch.class);

    private AsyncCallback.ChildrenCallback callback;



    public TopicWatch() {
        this.callback = new AsyncCallback.ChildrenCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                KeeperException.Code code = KeeperException.Code.get(rc);
                log.debug("ChildrenCallback code is {}", code);
                log.debug("ChildrenCallback children is {}", children);
                for (String c: children){
                    //查询每个主体下的订阅者并且监视这个主题,当订阅者发生变化时要修改本地列表
                }
            }
        };
    }

    @Override
    public void process(WatchedEvent event) {
        log.debug("topic watch,event is {}", event);
        ZooKeeper zk = ZkServer.getZookeeper();
        log.debug("continue watch");
        zk.getChildren(ZkConstant.Path.TOPICS, this, callback, null);

    }

}
