package com.liutaoyxz.yxzmq.cluster.zookeeper.callback;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/29
 * @Description: 某一队列下的消费者callback,当某一队列发生变化时,会重现查询这个队列的消费者列表,然后通过listener通知给broker
 */
public class QueueChildrenCallback implements AsyncCallback.ChildrenCallback, Watcher {

    public static final Logger log = LoggerFactory.getLogger(QueueChildrenCallback.class);

    private String queueName;

    private QueueChildrenCallback(String topicName) {
        this.queueName = topicName;
    }

    /**
     * 某一个主题下的订阅者发生变化后的回调,对订阅者进行分析,通过listener通知broker
     * @param rc
     * @param path
     * @param ctx
     * @param children
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        KeeperException.Code code = KeeperException.Code.get(rc);
        switch (code) {
            case OK:
                log.info("queue [{}] listen change,code is {},subscribers is {}", this.queueName,code,children);
                QueueCallback.addListeners(queueName,children);
                break;
            default:
                log.warn("queue [{}] listen change,code is {},subscribers is {}", this.queueName,code,children);
                break;
        }
    }

    public String getQueueName() {
        return queueName;
    }

    /**
     * 监视某一主题
     * @param queueName
     */
    public static void watchQueue(String queueName) {
        QueueChildrenCallback callback = new QueueChildrenCallback(queueName);
        ZooKeeper zk = ZkServer.getZookeeper();
        String path = ZkConstant.Path.QUEUES + "/" + queueName;
        log.info("watch queue [{}]",queueName);
        zk.getChildren(path, callback, callback, path);
    }


    /**
     * 监听到事件,采用异步的方式获得某一主题下的children
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        ZooKeeper zk = ZkServer.getZookeeper();
        log.info("watch event path is {}", path);
        Event.EventType type = event.getType();
        log.info("type is {},continue watch for topic {}", type, this.queueName);
        zk.getChildren(path, this, this, null);
    }



}