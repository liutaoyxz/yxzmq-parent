package com.liutaoyxz.yxzmq.cluster.zookeeper.watch;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import com.liutaoyxz.yxzmq.cluster.zookeeper.callback.TopicChildrenCallback;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 15:24 2017/11/29
 * @Description: 管理topic下面的所有订阅者 和所有的topic,当添加主题或者某个主题增加订阅者的时候需要通知broker进行操作
 */
public class TopicWatcher implements Watcher {

    public static final Logger log = LoggerFactory.getLogger(TopicWatcher.class);

    private AsyncCallback.ChildrenCallback callback;

    public TopicWatcher() {
        this.callback = new AsyncCallback.ChildrenCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                ZooKeeper zk = ZkServer.getZookeeper();

                KeeperException.Code code = KeeperException.Code.get(rc);
                log.info("ChildrenCallback code is {}", code);
                log.info("ChildrenCallback children is {}", children);
                for (String c : children) {
                    //查询每个主体下的订阅者并且监视这个主题,当订阅者发生变化时要修改本地列表
                    TopicWatcher.this.getTopicSubscriberies(c);
                }
                log.info("set watch for path {}", path);
                zk.exists(ZkConstant.Path.TOPICS, TopicWatcher.this, null, null);

            }
        };
    }

    @Override
    public void process(WatchedEvent event) {
        log.info("topic watch,event is {}", event);
        ZooKeeper zk = ZkServer.getZookeeper();
        zk.getChildren(ZkConstant.Path.TOPICS, this, callback, null);

    }

    /**
     * 异步获取某一个主题的children,并同时对这个主题进行监视
     * @param topicName
     * @return
     */
    private TopicChildrenCallback getTopicSubscriberies(String topicName) {
        log.info("set watch for topic {}", topicName);
        String path = ZkConstant.Path.TOPICS + "/" + topicName;
        ZooKeeper zk = ZkServer.getZookeeper();
        TopicChildrenCallback callback = new TopicChildrenCallback(topicName);
        zk.getChildren(path, new WatcherForTopic(topicName,callback), callback, null);
        return callback;
    }

    /**
     * 为一个固定的主题提供的watcher
     */
    class WatcherForTopic implements Watcher{

        private String topicName;

        private AsyncCallback.ChildrenCallback callback;

        public WatcherForTopic(String topicName, AsyncCallback.ChildrenCallback callback) {
            this.topicName = topicName;
            this.callback = callback;
        }

        @Override
        public void process(WatchedEvent event) {
            String path = event.getPath();
            ZooKeeper zk = ZkServer.getZookeeper();
            log.info("watch event path is {}",path);
            Event.EventType type = event.getType();
            log.info("type is {}",type);
            log.info("continue watch for topic {}",this.topicName);
            zk.getChildren(path,this,this.callback,null);
        }
    }

}
