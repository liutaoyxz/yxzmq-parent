package com.liutaoyxz.yxzmq.cluster.zookeeper.callback;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.TopicWatcher;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/29
 * @Description: 主题对应的callback,watch 到children发生变化之后采用异步的方式获取列表,回调broker 提供的listener
 */
public class TopicCallback implements AsyncCallback.ChildrenCallback {

    public static final Logger log = LoggerFactory.getLogger(TopicCallback.class);

    /**
     * 主题 和订阅者的映射
     */
    private static final ConcurrentHashMap<String,CopyOnWriteArrayList<String>> TOPIC_SUBSCRIBERS = new ConcurrentHashMap<>();

    private TopicWatcher watcher;

    public TopicCallback(TopicWatcher watcher) {
        this.watcher = watcher;
    }

    /**
     * /topics  的children 异步callback,对比本地保存的children判断出增加了哪一个主题
     * 完成后继续设置watcher,在watcher中调用异步callback,在这里不再添加callback,否则会出现无限循环
     *
     * @param rc
     * @param path
     * @param ctx
     * @param children
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        KeeperException.Code code = KeeperException.Code.get(rc);
        ZooKeeper zk = ZkServer.getZookeeper();
        switch (code){
            case OK:
                // 查询children成功
                if (children != null){
                    this.checkTopicsChildren(children);
                }

                log.info("get topics children OK,path is {},continue watch", path);
                zk.exists(ZkConstant.Path.TOPICS, watcher, null, null);

                break;
            case CONNECTIONLOSS:
                //连接错误,继续尝试,此时path 是null
                log.warn("get topics children CONNECTIONLOSS,path is {}",path);
                zk.exists(ZkConstant.Path.TOPICS, watcher, null, ctx);
                break;

            case NONODE:
                //没有这个节点,停止watch
                log.warn("get topics children NONODE,path is {}",path);
                break;
            default:
                log.warn("callback code not hit,code is {}",code);
                break;
        }
    }


    /**
     * 根据本地保存的children列表,检查出发生变化的topic并且监视这个topic
     * 这里监视到的是 /yxzmq/topics  下面的列表变化,并不是具体某一个主题订阅者的变化
     * @param children
     * @return
     */
    private List<String> checkTopicsChildren(List<String> children){
        Enumeration<String> keys = TOPIC_SUBSCRIBERS.keys();

        while (keys.hasMoreElements()){
            String topic = keys.nextElement();
            if (children.contains(topic)){
                children.remove(topic);
            }else {
                TOPIC_SUBSCRIBERS.remove(topic);
                // TODO: 2017/11/30 通知 broker 删除这个主题
                log.info("old topic deleted,topic is {}",topic);
            }
        }
        if (children.size() > 0){
            // TODO 新增的topic,设置异步监视
            log.info("there is new topic, new topic is {}",children);
            for (String child : children){
                TopicChildrenCallback.watchTopic(child);
            }
        }

        return null;
    }

    /**
     * 解析某一主题的订阅者,进行分析
     * @param topicName
     * @param subscribers
     */
    public static void addSubscribers(String topicName,List<String> subscribers){
        TOPIC_SUBSCRIBERS.put(topicName,new CopyOnWriteArrayList<>(subscribers));
        ZkBrokerRoot.getListener().topicSubscribersChange(topicName,subscribers);
    }


}
