package com.liutaoyxz.yxzmq.client;

import com.liutaoyxz.yxzmq.client.connection.YxzNettyConnection;
import com.liutaoyxz.yxzmq.client.connection.YxzNettyConnectionFactory;
import com.liutaoyxz.yxzmq.client.session.YxzNettySession;
import com.liutaoyxz.yxzmq.client.session.YxzNettyTopicPublisher;

import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @author Doug Tao
 * @Date: 14:53 2017/12/15
 * @Description: 上下文对象
 */
public class YxzClientContext {


    private YxzNettyConnection connection;

    private YxzNettyConnectionFactory factory;

    private YxzNettySession session;

    private Queue queue;

    private Topic topic;

    public YxzClientContext(YxzNettyConnectionFactory factory) {
        this.factory = factory;
    }

    public Queue queue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public Topic topic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public YxzNettyConnection connection() {
        return connection;
    }

    public YxzNettyConnectionFactory factory() {
        return factory;
    }

    public YxzNettySession session() {
        return session;
    }

    public void setConnection(YxzNettyConnection connection) {
        this.connection = connection;
    }

    public void setSession(YxzNettySession session) {
        this.session = session;
    }
}
