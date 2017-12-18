package com.liutaoyxz.yxzmq.client;

import com.liutaoyxz.yxzmq.client.connection.YxzNettyConnection;
import com.liutaoyxz.yxzmq.client.connection.YxzNettyConnectionFactory;
import com.liutaoyxz.yxzmq.client.session.YxzNettySession;

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

    public YxzClientContext createCtx(YxzNettyConnection conn){
        YxzClientContext newCtx = new YxzClientContext(this.factory);
        newCtx.connection = conn;
        return newCtx;
    }

    public YxzClientContext createCtx(YxzNettySession session){
        YxzClientContext newCtx = new YxzClientContext(this.factory);
        newCtx.connection = this.connection;
        newCtx.session = session;
        return newCtx;
    }

    public YxzClientContext createCtx(Queue queue){
        YxzClientContext newCtx = new YxzClientContext(this.factory);
        newCtx.connection = this.connection;
        newCtx.session = this.session;
        newCtx.queue = queue;
        return newCtx;
    }

    public YxzClientContext createCtx(Topic topic){
        YxzClientContext newCtx = new YxzClientContext(this.factory);
        newCtx.connection = this.connection;
        newCtx.session = this.session;
        newCtx.topic = topic;
        return newCtx;
    }

    public Queue queue() {
        return queue;
    }

    public Topic topic() {
        return topic;
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


}
