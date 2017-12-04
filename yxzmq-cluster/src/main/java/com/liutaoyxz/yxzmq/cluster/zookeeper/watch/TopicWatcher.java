package com.liutaoyxz.yxzmq.cluster.zookeeper.watch;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import com.liutaoyxz.yxzmq.cluster.zookeeper.callback.TopicCallback;
import com.liutaoyxz.yxzmq.cluster.zookeeper.callback.TopicChildrenCallback;
import org.apache.zookeeper.*;
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
        switch (state) {
            case Disconnected:
                //失去连接
                log.warn("watch topics,state is Disconnected");
                break;
            case Expired:
                //连接过期
                log.warn("watch topics,state is Expired");
                break;
            default:
                log.info("watch topics,state is {}", state);
                break;
        }
        switch (type) {
            case NodeChildrenChanged:
                //子节点变化
                log.info("topics childrenChanged,path is {}", path);
                zk.getChildren(ZkConstant.Path.TOPICS, this, callback, path);
                break;
//            case NodeDeleted:
//                //   /yxzmq/topics 节点被删除,不应该出现
//                break;
//
//            case NodeCreated:
//                //  /yxzmq/topics 节点被创建,也不应该出现
//                break;
//            case NodeDataChanged:
//                //  /yxzmq/topics 节点数据发生变化, 不应该出现
//
//                break;
            default:
                log.warn("topics watch type is not hit,type is {}", type);
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