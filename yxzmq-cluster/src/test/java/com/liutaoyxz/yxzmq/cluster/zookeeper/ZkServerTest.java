package com.liutaoyxz.yxzmq.cluster.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 15:57 2017/11/27
 * @Description: test
 */
public class ZkServerTest {

    public static final Logger log = LoggerFactory.getLogger(ZkServerTest.class);

    @Test
    public void getZookeeper() throws Exception {
        ZooKeeper zookeeper = ZkServer.getZookeeper();

        AsyncCallback.StatCallback callback = new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                switch (KeeperException.Code.get(rc)) {
                    case CONNECTIONLOSS:
                        log.info("连接错误");
                        break;
                    case NODEEXISTS:
//                        LOGGER.info("node exists");
                        break;
                    case OK:
                        log.info("创建节点--> {}", path);
                        break;
                    default:
                        break;
                }
            }
        };

        zookeeper.exists("/yxzmq", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println(event);
            }
        },callback,new Object());

        CountDownLatch latch = new CountDownLatch(1);
        latch.await();

    }

}