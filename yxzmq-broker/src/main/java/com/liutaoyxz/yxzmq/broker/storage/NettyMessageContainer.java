package com.liutaoyxz.yxzmq.broker.storage;

import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Doug Tao
 * @Date: 15:31 2017/12/8
 * @Description: 保存queue 消息,和 客户端和主题 的对应关系
 */
public class NettyMessageContainer {


    private static final Logger log = LoggerFactory.getLogger(NettyMessageContainer.class);

    /**
     * queue 消息的订阅者
     */
    private static final ConcurrentHashMap<String,BlockingQueue<ServerClient>> QUEUE_LISTENERS = new ConcurrentHashMap<>();

    /**
     * topic 订阅者
     */
    private static final ConcurrentHashMap<String,List<ServerClient>> TOPIC_SUBSCRIBERS = new ConcurrentHashMap<>();


    /**
     * 设置 某一主题的订阅者
     * @param topicName
     * @param subscribers
     */
    public static void setTopicSubscribers(String topicName, List<ServerClient> subscribers){
        if (StringUtils.isBlank(topicName)){
            return;
        }
        log.info("set topic subscribers,queue name is [{}] , subscribers is {}",topicName,subscribers);
        TOPIC_SUBSCRIBERS.put(topicName,subscribers);
    }

    /**
     * 设置某一 name下的 queue 监听者
     * @param queueName
     * @param listeners
     */
    public static void setQueueListeners(String queueName,BlockingQueue<ServerClient> listeners){
        if (StringUtils.isBlank(queueName)){
            return;
        }
        log.info("set queue listeners,queue name is [{}] , listeners is {}",queueName,listeners);
        QUEUE_LISTENERS.put(queueName,listeners);
    }


}
