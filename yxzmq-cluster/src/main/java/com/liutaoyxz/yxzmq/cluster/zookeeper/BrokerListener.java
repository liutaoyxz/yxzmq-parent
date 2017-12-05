package com.liutaoyxz.yxzmq.cluster.zookeeper;

import java.util.List;

/**
 * @author Doug Tao
 * @Date 下午9:22 2017/12/4
 * @Description: broker 的回调,zookeeper监视到事件之后调用
 */
public interface BrokerListener {

    /**
     * subject 发生变化
     * @param subjectName
     */
    void subjectChange(String subjectName);

    /**
     * mirror 发生变化
     * @param mirrorName
     */
    void mirrorChange(String mirrorName);

    /**
     * 某一主题订阅者发生变化
     * @param topicName
     * @param subscribers
     */
    void topicSubscribersChange(String topicName, List<String> subscribers);

    /**
     * 某一队列的消费者发生变化
     * @param queueName
     * @param listeners
     */
    void queueListenersChange(String queueName,List<String> listeners);

    /**
     * 设置我自己在zookeeper的名字
     * @param myName
     */
    void setMyName(String myName);

}
