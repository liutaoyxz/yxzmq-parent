package com.liutaoyxz.yxzmq.broker.messagehandler;

import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Doug Tao
 * @Date: 17:05 2017/11/17
 * @Description: 消息容器,保存消息/客户端, 消息内容 等信息
 */
public class MessageContainer {

    /**
     * 主题,消息队列
     */
    private static final ConcurrentHashMap<String,BlockingQueue> TOPIC_QUEUE = new ConcurrentHashMap<>();

    /**
     * 点对点模式,消息队列
     */
    private static final ConcurrentHashMap<String,BlockingQueue> PP_QUEUE  = new ConcurrentHashMap<>();


}
