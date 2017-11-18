package com.liutaoyxz.yxzmq.broker.storage;

import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import com.liutaoyxz.yxzmq.io.wrap.TopicMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Doug Tao
 * @Date: 17:05 2017/11/17
 * @Description: 消息容器,保存消息/客户端, 消息内容 等信息
 */
public class MessageContainer {

    /**
     * 主题,消息队列
     */
    private static final ConcurrentHashMap<String,BlockingQueue<TopicMessage>> TOPIC_HOUSE = new ConcurrentHashMap<>();

    /**
     * 点对点模式,消息队列
     */
    private static final ConcurrentHashMap<String,BlockingQueue<QueueMessage>> PP_HOUSE  = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(MessageContainer.class);

    /**
     * 保存主题消息
     * @param title 主题
     * @param message 主题消息包装类
     * @return 保存是否成功
     * TODO 主题消息不应该保存,占用存储空间. 最后改造成为保存到日志中,在持久化中完成这个功能
     */
    public static boolean save(String title,TopicMessage message){
        checkTitle(title);
        BlockingQueue<TopicMessage> queue = TOPIC_HOUSE.get(title);
        if (queue == null){
            queue = new LinkedBlockingDeque<>();
            TOPIC_HOUSE.putIfAbsent(title,queue);
            queue = TOPIC_HOUSE.get(title);
        }
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            log.debug("save message error",e);
            return false;
        }
        log.debug("receive message {}",message);
        return true;
    }

    /**
     * 保存p2p消息
     * @param title 主题
     * @param message 消息包装类
     * @return 保存是否成功
     */
    public static boolean save(String title,QueueMessage message){
        checkTitle(title);
        BlockingQueue<QueueMessage> queue = PP_HOUSE.get(title);
        if (queue == null){
            queue = new LinkedBlockingDeque<>();
            PP_HOUSE.putIfAbsent(title,queue);
            queue = PP_HOUSE.get(title);
        }
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            log.debug("save message error",e);
            return false;
        }
        log.debug("receive message {}",message);
        return true;
    }

    private static void checkTitle(String title){
        if (StringUtils.isBlank(title)){
            throw new NullPointerException("message title can not be blank");
        }
    }

}
