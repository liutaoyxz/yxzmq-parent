package com.liutaoyxz.yxzmq.client.session;

import com.liutaoyxz.yxzmq.client.YxzClientContext;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @author Doug Tao
 * @Date: 16:28 2017/12/15
 * @Description:
 */
public class SessionTask {

    /**发布消息**/
    static final int PUBLISH = 1;

    /**订阅主题**/
    static final int SUBSCRIBE = 2;

    /**监听queue**/
    static final int LISTEN_QUEUE = 3;

    /** 发送queue 消息 **/
    static final int QUEUE_SEND = 4;

    /** 关闭session **/
    static final int SESSION_CLOSE = -1;

    /** 任务类型 **/
    private int type;

    /** 上下文 **/
    private YxzClientContext ctx;

    /** 消息 **/
    private Message message;

    private Topic topic;

    private Queue queue;

    public SessionTask(int type) {
        this.type = type;
        this.ctx = ctx;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }
}
