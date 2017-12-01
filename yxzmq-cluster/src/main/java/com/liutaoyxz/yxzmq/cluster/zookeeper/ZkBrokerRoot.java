package com.liutaoyxz.yxzmq.cluster.zookeeper;

import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.broker.BrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.BrokerWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.QueueWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.TopicWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 10:23 2017/11/28
 * @Description: 当有新连接加到brokers 的时候需要修改备份策略, 当前broker的主体需要接受全部上游的数据,下游的broker需要清理之前的数据然后接受当前broker的数据
 * broker 先注册到zookeeper,声明状态为not_ready, 然后遍历所有broker,查看状态,生产ready列表后修改自己的状态为ready
 */
public class ZkBrokerRoot implements BrokerRoot {


    public static final Logger log = LoggerFactory.getLogger(ZkBrokerRoot.class);

    /**
     * broker 锁,更新broker和镜像同步信息时需要获得锁
     */
    private ReentrantLock brokerLock = new ReentrantLock();

    /**
     * ready 的broker列表
     */
    private static final CopyOnWriteArrayList<Broker> READY_BROKER = new CopyOnWriteArrayList<>();

    /**
     * 已经注册,但是没有ready的broker.  key-broker name
     */
    private static final ConcurrentHashMap<String,Broker> NOT_READY_BROKER = new ConcurrentHashMap<>();



    /**
     * 本机的broker对象
     */
    private Broker self;

    private int port;

    public ZkBrokerRoot(int port) {
        this.port = port;
    }

    @Override
    public boolean checkRoot() {

        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            Stat stat = zk.exists(ZkConstant.Path.ROOT, false);
            if (stat == null){
                //没有 根,创建
                log.info("checkRoot,root is null");
                String rootCreate = zk.create(ZkConstant.Path.ROOT, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("root create result is {}",rootCreate);
                String brokersCreate = zk.create(ZkConstant.Path.BROKERS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("brokers create result is {}",brokersCreate);
                String clientsCreate = zk.create(ZkConstant.Path.CLIENTS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("clients create result is {}",clientsCreate);
                String topicsCreate = zk.create(ZkConstant.Path.TOPICS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("topics create result is {}",topicsCreate);
                String queuesCreate = zk.create(ZkConstant.Path.QUEUES, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("queues create result is {}",queuesCreate);
            }else {
                //检查 其他4个目录
                Stat brokersStat = zk.exists(ZkConstant.Path.BROKERS, false);
                if(brokersStat == null){
                    String brokersCreate = zk.create(ZkConstant.Path.BROKERS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.info("brokers create result is {}",brokersCreate);
                }
                Stat clientsStat = zk.exists(ZkConstant.Path.CLIENTS, false);
                if (clientsStat == null){
                    String clientsCreate = zk.create(ZkConstant.Path.CLIENTS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.info("clients create result is {}",clientsCreate);
                }
                Stat topicsStat = zk.exists(ZkConstant.Path.TOPICS, false);
                if (topicsStat == null){
                    String topicsCreate = zk.create(ZkConstant.Path.TOPICS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.info("topics create result is {}",topicsCreate);
                }
                Stat queuesStat = zk.exists(ZkConstant.Path.QUEUES, false);
                if (queuesStat == null){
                    String queuesCreate = zk.create(ZkConstant.Path.QUEUES, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.info("queues create result is {}",queuesCreate);
                }

                log.info("brokersStat is {},clientsStat is {},topicsStat is {},queuesStat is {}",
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
    public boolean register() {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            log.info("hostAddress is {}",hostAddress);
            String preBrokerName = hostAddress+":"+port + "-";
            String createPath = zk.create(ZkConstant.Path.BROKERS + "/" + preBrokerName, ZkConstant.BrokerState.NOT_READY.getBytes(Charset.forName("utf-8")), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            String brokerName = createPath.replace(ZkConstant.Path.BROKERS + "/","");
            log.info("create broker brokerName is {}",brokerName);
            this.self = new Broker(brokerName);
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
    public List<String> brokers() throws InterruptedException {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            List<String> children = zk.getChildren(ZkConstant.Path.BROKERS, new BrokerWatcher());
            return children;
        } catch (KeeperException e) {
            log.error("get brokers error",e);
        }
        return null;
    }

    @Override
    public Broker getBroker() {
        return this.self;
    }

    @Override
    public void start() throws InterruptedException {
        checkRoot();
        ZooKeeper zk = ZkServer.getZookeeper();

        TopicWatcher topicWatch = TopicWatcher.getWatcher();
        QueueWatcher queueWatcher = QueueWatcher.getWatcher();
        BrokerWatcher brokerWatcher = new BrokerWatcher();
        //getChildren 采用同步等待
        try {
            //遍历topics
            List<String> topicChildren = zk.getChildren(ZkConstant.Path.TOPICS, topicWatch);
            log.info("topics children {}",topicChildren);
            topicWatch.watchChildren(topicChildren);
            //遍历queues
            List<String> queueChildren = zk.getChildren(ZkConstant.Path.QUEUES, queueWatcher);
            log.info("queues children {}",topicChildren);
            queueWatcher.watchChildren(queueChildren);

            //获取brokers下的 children
            List<String> brokerChildren = zk.getChildren(ZkConstant.Path.BROKERS, brokerWatcher);
            log.info("brokers children {}",brokerChildren);

            register();
            ready();
        }catch (KeeperException.ConnectionLossException e){
            log.info("connection loss",e);
        }catch (KeeperException e) {
            log.info("get children error",e);
        }

    }

    /**
     * 设置broker的状态为ready
     */
    private void ready() throws KeeperException, InterruptedException {
        ZooKeeper zk = ZkServer.getZookeeper();
        String brokerName = this.self.getName();
        String path = ZkConstant.Path.BROKERS + "/" + brokerName;
        zk.setData(path,ZkConstant.BrokerState.READY.getBytes(Charset.forName("utf-8")),0);
        log.info("broker ready,broker name is {}",brokerName);
    }


}
