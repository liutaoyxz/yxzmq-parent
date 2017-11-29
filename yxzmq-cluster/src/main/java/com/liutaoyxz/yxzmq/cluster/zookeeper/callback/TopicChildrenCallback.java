package com.liutaoyxz.yxzmq.cluster.zookeeper.callback;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/29
 * @Description: 某一个主题下面的订阅者发生变化的callback
 */
public class TopicChildrenCallback implements AsyncCallback.ChildrenCallback {

    public static final Logger log = LoggerFactory.getLogger(TopicChildrenCallback.class);

    /**
     * 主题 和订阅者的映射
     */
    private static final ConcurrentHashMap<String,List<String>> TOPIC_SUBSCRIBERIES = new ConcurrentHashMap<>();

    private String topicName;

    public TopicChildrenCallback(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        KeeperException.Code code = KeeperException.Code.get(rc);
        switch (code){


            default:
                log.warn("callback code not hit,code is {}",code);
                break;
        }
    }

    public String getTopicName() {
        return topicName;
    }
}
