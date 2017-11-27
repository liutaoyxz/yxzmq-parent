package com.liutaoyxz.yxzmq.cluster.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Doug Tao
 * @Date: 15:45 2017/11/27
 * @Description: zk server ,提供模块使用的zkserver 连接对象
 */
public class ZkServer {


    private static ZooKeeper zooKeeper;

    private static int timeout = 15000;

    private static String connectStr = "127.0.0.1:2181";

    public static final Logger log = LoggerFactory.getLogger(ZkServer.class);

    public static ZooKeeper getZookeeper(){
        if (zooKeeper == null){
            synchronized (ZkServer.class){
                if (zooKeeper == null){
                    try {
                        zooKeeper = new ZooKeeper(connectStr,timeout,new ServerWatcher());
                    } catch (IOException e) {
                        log.info("create zookeeper error",e);
                    }
                }
            }
        }
        return zooKeeper;
    }

    static class ServerWatcher implements Watcher{

        @Override
        public void process(WatchedEvent event) {
            log.debug("server event :{}",event);
        }
    }


}
