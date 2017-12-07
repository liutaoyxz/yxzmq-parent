package com.liutaoyxz.yxzmq.cluster.zookeeper.callback;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/29
 * @Description: 某一个主题下面的订阅者发生变化的callback
 */
public class TopicChildrenCallback implements AsyncCallback.ChildrenCallback, Watcher {

    public static final Logger log = LoggerFactory.getLogger(TopicChildrenCallback.class);

    private String topicName;

    private TopicChildrenCallback(String topicName) {
        this.topicName = topicName;
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
        ZooKeeper zk = ZkServer.getZookeeper();
        switch (code) {
            case OK:
                log.info("topic [{}] subscriber change,code is {},subscribers is {}", this.topicName,code,children);
                TopicCallback.addSubscribers(topicName,children);
                break;
            case CONNECTIONLOSS:
                //连接出错
                log.warn("get topic [{}] children connection less");
                break;
            default:
                log.warn("topic [{}] subscriber change,code is {},subscribers is {}", this.topicName,code,children);
                break;
        }
    }

    public String getTopicName() {
        return topicName;
    }

    /**
     * 监视某一主题
     * @param topicName
     * @param first 是否是第一次,第一次需要初始化监视列表
     */
    public static void watchTopic(String topicName) {
        TopicChildrenCallback callback = new TopicChildrenCallback(topicName);
        ZooKeeper zk = ZkServer.getZookeeper();
        String path = ZkConstant.Path.TOPICS + "/" + topicName;
        log.info("watch topic [{}]",topicName);
        zk.getChildren(path, callback, callback, path);
    }


    /**
     * 监听到事件,采用异步的方式获得某一主题下的children
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        String path = ZkConstant.Path.TOPICS + "/" + topicName;
        ZooKeeper zk = ZkServer.getZookeeper();
        log.info("watch event path is {}", path);
        Event.EventType type = event.getType();
        Event.KeeperState state = event.getState();
        log.info("type is {},continue watch for topic {}", type, this.topicName);
        switch (type){
            case None:
                //连接状态发生变化
                log.warn("path [{}] watch state change,state is {}",path,state);
                switch (state){
                    case Expired:
                        //连接过期,重新创建zookeeper对象,重新连接到集群
                        ZkBrokerRoot.restart();
                    default:
                        zk.getChildren(path, this, this, path);
                        break;
                }
                break;
            case NodeChildrenChanged:
                //topic 下面的订阅者发生变化
                zk.getChildren(path, this, this, path);
                break;
            default:
                log.warn("watch topic children,type not case,type is {}",type);
                zk.getChildren(path, this, this, path);
                break;
        }
    }



}
