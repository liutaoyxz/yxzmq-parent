package com.liutaoyxz.yxzmq.cluster.zookeeper;

import com.liutaoyxz.yxzmq.cluster.broker.BrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.BrokerWatch;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Doug Tao
 * @Date: 10:23 2017/11/28
 * @Description:
 */
public class ZkBrokerRoot implements BrokerRoot {


    public static final Logger log = LoggerFactory.getLogger(ZkBrokerRoot.class);


    @Override
    public boolean checkRoot() {

        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            Stat stat = zk.exists(ZkConstant.Path.ROOT, false);
            if (stat == null){
                //没有 根,创建
                log.debug("checkRoot,root is null");
                String rootCreate = zk.create(ZkConstant.Path.ROOT, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.debug("root create result is {}",rootCreate);
                String brokersCreate = zk.create(ZkConstant.Path.BROKERS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.debug("brokers create result is {}",brokersCreate);
                String clientsCreate = zk.create(ZkConstant.Path.CLIENTS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.debug("clients create result is {}",clientsCreate);
                String topicsCreate = zk.create(ZkConstant.Path.TOPICS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.debug("topics create result is {}",topicsCreate);
                String queuesCreate = zk.create(ZkConstant.Path.QUEUES, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.debug("queues create result is {}",queuesCreate);
            }else {
                //检查 其他4个目录
                Stat brokersStat = zk.exists(ZkConstant.Path.BROKERS, false);
                if(brokersStat == null){
                    String brokersCreate = zk.create(ZkConstant.Path.BROKERS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.debug("brokers create result is {}",brokersCreate);
                }
                Stat clientsStat = zk.exists(ZkConstant.Path.CLIENTS, false);
                if (clientsStat == null){
                    String clientsCreate = zk.create(ZkConstant.Path.CLIENTS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.debug("clients create result is {}",clientsCreate);
                }
                Stat topicsStat = zk.exists(ZkConstant.Path.TOPICS, false);
                if (topicsStat == null){
                    String topicsCreate = zk.create(ZkConstant.Path.TOPICS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.debug("topics create result is {}",topicsCreate);
                }
                Stat queuesStat = zk.exists(ZkConstant.Path.QUEUES, false);
                if (queuesStat == null){
                    String queuesCreate = zk.create(ZkConstant.Path.QUEUES, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.debug("queues create result is {}",queuesCreate);
                }

                log.debug("brokersStat is {},clientsStat is {},topicsStat is {},queuesStat is {}",
                        brokersStat,clientsStat,topicsStat,queuesStat);
            }
            return true;
        } catch (KeeperException e) {
            log.error("check root error",e);
        } catch (InterruptedException e) {
            log.error("check root error",e);
        }
        return false;
    }

    @Override
    public boolean register(int port) {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            log.debug("hostAddress is {}",hostAddress);
            String brokerName = hostAddress+":"+port + "-";
            String createPath = zk.create(ZkConstant.Path.BROKERS + "/" + brokerName, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.info("create broker path is {}",createPath);
        } catch (UnknownHostException e) {
            log.error("register zookeeper error",e);
        } catch (InterruptedException e) {
            log.error("register zookeeper error",e);
        } catch (KeeperException e) {
            log.error("register zookeeper error",e);
        }
        return false;
    }

    @Override
    public List<String> brokers() {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            List<String> children = zk.getChildren(ZkConstant.Path.BROKERS, new BrokerWatch());
            return children;
        } catch (KeeperException e) {
            log.error("get brokers error",e);
        } catch (InterruptedException e) {
            log.error("get brokers error",e);
        }
        return null;
    }
}
