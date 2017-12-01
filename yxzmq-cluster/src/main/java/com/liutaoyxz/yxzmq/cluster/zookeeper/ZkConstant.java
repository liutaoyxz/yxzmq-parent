package com.liutaoyxz.yxzmq.cluster.zookeeper;

/**
 * @author Doug Tao
 * @Date: 10:30 2017/11/28
 * @Description:
 */
public interface ZkConstant {

    interface Path{

        String ROOT = "/yxzmq";

        String BROKERS = ROOT + "/brokers";

        String CLIENTS = ROOT + "/clients";

        String TOPICS = ROOT + "/topics";

        String QUEUES = ROOT + "/queues";

    }

    interface BrokerState{

        String NOT_READY = "notReady";

        String READY = "ready";

    }


}
