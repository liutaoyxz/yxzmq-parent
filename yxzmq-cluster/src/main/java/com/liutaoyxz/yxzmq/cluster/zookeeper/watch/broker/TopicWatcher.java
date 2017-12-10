package com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 15:24 2017/11/29
 * @Description: watch topics下面的所有主题的变化,当主题变化之后需要监视主题
 */
public class TopicWatcher implements Watcher {

    public static final Logger log = LoggerFactory.getLogger(TopicWatcher.class);

    private TopicCallback callback;

    private TopicWatcher() {
    }

    public static TopicWatcher getWatcher() {
        TopicWatcher watcher = new TopicWatcher();
        watcher.callback = new TopicCallback(watcher);
        return watcher;
    }

    @Override
    public void process(WatchedEvent event) {
        log.debug("topics watch,event is {}", event);
        String path = event.getPath();
        Event.EventType type = event.getType();
        Event.KeeperState state = event.getState();
        ZooKeeper zk = ZkServer.getZookeeper();

        switch (type) {
            case NodeChildrenChanged:
                //子节点变化
                log.info("topics childrenChanged,path is {}", path);
                zk.getChildren(ZkConstant.Path.TOPICS, this, callback, path);
                break;

            case None:
                log.warn("watch topics,state change,state is {}",state);
                switch (state) {
                    case Expired:
                        //连接过期
                        log.info("zookeeper expired,restart zookeeper");
                        ZkBrokerRoot.restart(ZkServer.getZkVersion());
                        break;
                    default:
                        zk.getChildren(ZkConstant.Path.TOPICS, this, callback, path);
                        break;
                }
                break;
            default:
                log.warn("topics watch type is not hit,type is {}", type);
                zk.getChildren(ZkConstant.Path.TOPICS, this, callback, path);
                break;
        }

    }

    /**
     * 启动是调用,监视每一个topic
     *
     * @param children
     */
    public void watchChildren(List<String> children){
        for (String child : children) {
            TopicChildrenCallback.watchTopic(child);
        }
    }


}
