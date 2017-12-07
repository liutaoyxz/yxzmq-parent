package com.liutaoyxz.yxzmq.cluster.zookeeper.watch;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import com.liutaoyxz.yxzmq.cluster.zookeeper.callback.QueueCallback;
import com.liutaoyxz.yxzmq.cluster.zookeeper.callback.QueueChildrenCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 15:24 2017/11/29
 * @Description:
 */
public class QueueWatcher implements Watcher {

    public static final Logger log = LoggerFactory.getLogger(TopicWatcher.class);

    private QueueCallback callback;

    private QueueWatcher() {
    }

    public static QueueWatcher getWatcher() {
        QueueWatcher watcher = new QueueWatcher();
        watcher.callback = new QueueCallback(watcher);
        return watcher;
    }

    @Override
    public void process(WatchedEvent event) {
        log.debug("queues watch,event is {}", event);
        String path = event.getPath();
        Event.EventType type = event.getType();
        Event.KeeperState state = event.getState();
        ZooKeeper zk = ZkServer.getZookeeper();
        switch (type) {
            case NodeChildrenChanged:
                //子节点变化
                log.info("queues childrenChanged,path is {}", path);
                zk.getChildren(ZkConstant.Path.QUEUES, this, callback, ZkConstant.Path.QUEUES);
                break;
            case None:
                //连接状态发生变化,此时path 是null
                log.warn("connect state change,state is {}",state);
                switch (state){
                    case Expired:
                        log.info("zookeeper expired,restart zookeeper");
                        ZkBrokerRoot.restart(ZkServer.getZkVersion());
                        break;
                    default:
                        zk.getChildren(ZkConstant.Path.QUEUES, this, callback, ZkConstant.Path.QUEUES);
                        break;
                }
                break;
            case NodeDeleted:
                //   /yxzmq/queues 节点被删除,不应该出现
                break;

            case NodeCreated:
                //  /yxzmq/queues 节点被创建,也不应该出现
                break;
            case NodeDataChanged:
                //  /yxzmq/queues 节点数据发生变化, 不应该出现

                break;
            default:
                log.warn("queues watch type is not hit,type is {}", type);
                break;
        }

    }

    /**
     * 启动是调用,监视每一个queue
     *
     * @param children
     */
    public void watchChildren(List<String> children){
        for (String child : children) {
            QueueChildrenCallback.watchQueue(child);
        }
    }

}
