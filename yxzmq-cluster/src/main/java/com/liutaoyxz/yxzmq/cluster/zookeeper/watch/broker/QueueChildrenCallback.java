package com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/29
 * @Description: 某一队列下的消费者callback,当某一队列发生变化时,会重现查询这个队列的消费者列表,然后通过listener通知给broker
 */
public class QueueChildrenCallback implements AsyncCallback.ChildrenCallback, Watcher {

    public static final Logger log = LoggerFactory.getLogger(QueueChildrenCallback.class);

    private static final Set WATCHING = new CopyOnWriteArraySet();

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
                log.info("queue [{}] listen change,code is {},listeners is {}", this.queueName,code,children);
                if (children == null){
                    children = new ArrayList<>();
                }
                ZkBrokerRoot.getListener().queueListenersChange(queueName,children);
                break;
            default:
                log.warn("queue [{}] listen change,code is {},listeners is {}", this.queueName,code,children);
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
    public synchronized static void watchQueue(String queueName) {
        if (WATCHING.contains(queueName)){
            return;
        }
        QueueChildrenCallback callback = new QueueChildrenCallback(queueName);
        ZooKeeper zk = ZkServer.getZookeeper();
        String path = ZkConstant.Path.QUEUES + "/" + queueName;
        log.debug("watch queue [{}]",queueName);
        zk.getChildren(path, callback, callback, path);
    }


    /**
     * 监听到事件,采用异步的方式获得某一主题下的children
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        String path = ZkConstant.Path.QUEUES + "/" + this.queueName;
        ZooKeeper zk = ZkServer.getZookeeper();
        log.debug("watch event path is {}", path);
        Event.EventType type = event.getType();
        Event.KeeperState state = event.getState();
        log.debug("type is {},continue watch for queue {}", type, this.queueName);
        switch (type){
            case None:
                //连接状态发生变化
                log.warn("path [{}] watch state change,state is {}",path,state);
                switch (state){
                    case Expired:
                        log.info("zookeeper expired,restart zookeeper");
                        ZkBrokerRoot.restart(ZkServer.getZkVersion());
                        break;
                    case Disconnected:
                        zk.getChildren(path, this, this, path);
                        break;
                    default:
                        log.warn("watch queue [{}] state is {}",this.queueName,state);
                        zk.getChildren(path, this, this, path);
                        break;
                }
                break;
            case NodeChildrenChanged:
                //queue 下面的订阅者发生变化
                zk.getChildren(path, this, this, path);
                break;
            default:
                log.warn("watch topic children,type not case,type is {}",type);
                zk.getChildren(path, this, this, path);
                break;
        }
    }



}
