package com.liutaoyxz.yxzmq.broker.client;

import com.liutaoyxz.yxzmq.broker.storage.NettyMessageContainer;
import com.liutaoyxz.yxzmq.cluster.zookeeper.BrokerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Doug Tao
 * @Date 下午9:10 2017/12/5
 * @Description:
 */
public class BrokerZkListener implements BrokerListener {

    public static final Logger log = LoggerFactory.getLogger(BrokerZkListener.class);

    @Override
    public void subjectChange(String subjectName) {

    }

    @Override
    public void mirrorChange(String mirrorName) {
        try {
            ServerClientManager.setMirror(mirrorName);
        } catch (InterruptedException e) {
            log.error("set mirrorName error",e);
        } catch (IOException e) {
            log.error("set mirrorName error",e);
        }
    }

    @Override
    public void topicSubscribersChange(String topicName, List<String> subscribers) {
        List<ServerClient> clients = ServerClientManager.getServerClients(subscribers);
        NettyMessageContainer.setTopicSubscribers(topicName,clients);
    }

    /**
     * 队列的监听发生变化,如果客户端先监听的队列,broker端 是后来动态连接上来的,此时broker可能会差找不到listener对象
     *
     * @param queueName
     * @param listeners
     */
    @Override
    public void queueListenersChange(String queueName, List<String> listeners) {
        NettyMessageContainer.setQueueListeners(queueName,listeners);
    }

    @Override
    public void setMyName(String myName) {
        ServerClientManager.setMyName(myName);
    }
}