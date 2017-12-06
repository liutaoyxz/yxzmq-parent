package com.liutaoyxz.yxzmq.broker.client;

import com.liutaoyxz.yxzmq.cluster.zookeeper.BrokerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        }
    }

    @Override
    public void topicSubscribersChange(String topicName, List<String> subscribers) {

    }

    @Override
    public void queueListenersChange(String queueName, List<String> listeners) {

    }

    @Override
    public void setMyName(String myName) {
        ServerClientManager.setMyName(myName);
    }
}
