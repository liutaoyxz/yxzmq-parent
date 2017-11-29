package com.liutaoyxz.yxzmq.cluster.zookeeper.callback;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/29
 * @Description: 主题对应的callback,watch 到children发生变化之后采用异步的方式获取列表,回调broker 提供的listener
 */
public class TopicChildrenCallback implements AsyncCallback.ChildrenCallback {

    public static final Logger log = LoggerFactory.getLogger(TopicChildrenCallback.class);

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
