package com.liutaoyxz.yxzmq.cluster.zookeeper;

import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.client.ClientBrokerChildrenWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.client.ClientBrokerWatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 10:23 2017/11/28
 */
public class ZkClientRoot {


    public static final Logger log = LoggerFactory.getLogger(ZkClientRoot.class);

    /**
     * broker 锁,更新broker和镜像同步信息时需要获得锁
     */
    private static ReentrantLock restartLock = new ReentrantLock();

    /**
     * ready 的broker列表
     */
    private static final CopyOnWriteArrayList<Broker> READY_BROKER = new CopyOnWriteArrayList<>();

    /**
     * connected 的broker列表,已经连接的brokers
     */
    private static final CopyOnWriteArrayList<Broker> CONNECTED_BROKER = new CopyOnWriteArrayList<>();

    /**
     * 所有的broker  key-broker name
     */
    private static final ConcurrentHashMap<String, Broker> ALL_BROKER = new ConcurrentHashMap<>();

    private static int zkVersion = 1;

    private ClientListener listener;

    private ZkClientRoot(ClientListener listener) {
        this.listener = listener;
    }

    private String myName;

    public synchronized static ZkClientRoot createRoot(ClientListener listener, String zookeeperConnectStr) throws IOException {
        ZkServer.createZookeeper(zookeeperConnectStr);
        ZkClientRoot root = new ZkClientRoot(listener);
        return root;
    }


    private boolean checkRoot() throws Exception {

        ZooKeeper zk = ZkServer.getZookeeper();
        Stat stat = zk.exists(ZkConstant.Path.ROOT, false);
        if (stat == null) {
            //没有 根,创建
            log.info("checkRoot,root is null");
            String rootCreate = zk.create(ZkConstant.Path.ROOT, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            log.info("root create result is {}", rootCreate);
            String brokersCreate = zk.create(ZkConstant.Path.BROKERS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            log.info("brokers create result is {}", brokersCreate);
            String clientsCreate = zk.create(ZkConstant.Path.CLIENTS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            log.info("clients create result is {}", clientsCreate);
            String topicsCreate = zk.create(ZkConstant.Path.TOPICS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            log.info("topics create result is {}", topicsCreate);
            String queuesCreate = zk.create(ZkConstant.Path.QUEUES, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            log.info("queues create result is {}", queuesCreate);
        } else {
            //检查 其他4个目录
            Stat brokersStat = zk.exists(ZkConstant.Path.BROKERS, false);
            if (brokersStat == null) {
                String brokersCreate = zk.create(ZkConstant.Path.BROKERS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("brokers create result is {}", brokersCreate);
            }
            Stat clientsStat = zk.exists(ZkConstant.Path.CLIENTS, false);
            if (clientsStat == null) {
                String clientsCreate = zk.create(ZkConstant.Path.CLIENTS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("clients create result is {}", clientsCreate);
            }
            Stat topicsStat = zk.exists(ZkConstant.Path.TOPICS, false);
            if (topicsStat == null) {
                String topicsCreate = zk.create(ZkConstant.Path.TOPICS, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("topics create result is {}", topicsCreate);
            }
            Stat queuesStat = zk.exists(ZkConstant.Path.QUEUES, false);
            if (queuesStat == null) {
                String queuesCreate = zk.create(ZkConstant.Path.QUEUES, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info("queues create result is {}", queuesCreate);
            }

            log.info("\rbrokersStat is {}\rclientsStat is {}\rtopicsStat is {}\rqueuesStat is {}",
                    brokersStat, clientsStat, topicsStat, queuesStat);
        }
        return true;
    }

    private boolean register() throws Exception {
        ZooKeeper zk = ZkServer.getZookeeper();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        log.info("hostAddress is {}", hostAddress);
        String createPath = zk.create(ZkConstant.Path.CLIENTS + "/" + hostAddress + "-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        String clientName = createPath.replace(ZkConstant.Path.CLIENTS + "/", "");
        log.info("register client clientName is {}", clientName);
        this.myName = clientName;
        return false;
    }


    public List<Broker> start() throws Exception {
        List<Broker> result = new ArrayList<>();
        checkRoot();
        ZooKeeper zk = ZkServer.getZookeeper();
        ClientBrokerWatcher clientBrokerWatcher = new ClientBrokerWatcher(this);
        //获取brokers下的 children,第一次获得到children之后遍历children的数据,判断是否为ready,采用同步的方式
        List<String> brokerChildren = zk.getChildren(ZkConstant.Path.BROKERS, clientBrokerWatcher);
        log.info("brokers {}", brokerChildren);
        createBrokers(brokerChildren);
        register();
        result.addAll(READY_BROKER);
        return result;
    }

    /**
     * 连接过期后重启zookeeper 并且重新
     */
    public void restart(int version) {
        restartLock.lock();
        if (version < zkVersion) {
            return;
        }
        try {
            zkVersion = ZkServer.reCreateZookeeper();
            start();
        } catch (Exception e) {
            log.error("restart zookeeper error", e);
        } finally {
            restartLock.unlock();
        }
    }


    /**
     * broker 状态为ready,添加到ready 列表,重新建立关系
     */
    public void brokerStateChange(String brokerName, String newState) {
        Broker broker = ALL_BROKER.get(brokerName);
        if (StringUtils.equals(ZkConstant.BrokerState.READY, newState)) {
            //ready
            if (READY_BROKER.contains(broker)) {
                return;
            } else {
                broker.ready();
                READY_BROKER.add(broker);
                connectBrokers();
            }
        } else {
            if (READY_BROKER.contains(broker)) {
                broker.notReady();
                boolean remove = READY_BROKER.remove(broker);
                if (remove) {
                    connectBrokers();
                }
            }
        }

    }

    private void createBrokers(List<String> children) throws InterruptedException {
        ZooKeeper zk = ZkServer.getZookeeper();
        if (children != null) {
            for (String child : children) {
                String path = ZkConstant.Path.BROKERS + "/" + child;
                ClientBrokerChildrenWatcher bcw = new ClientBrokerChildrenWatcher(child,this);
                try {
                    byte[] data = zk.getData(path, bcw, null);
                    if (data != null) {
                        String dataStr = new String(data, Charset.forName("utf-8"));
                        if (StringUtils.equals(ZkConstant.BrokerState.NOT_READY, dataStr)) {
                            //没准备好
                            Broker broker = new Broker(child);
                            ALL_BROKER.put(child, broker);
                        } else if (StringUtils.equals(ZkConstant.BrokerState.READY, dataStr)) {
                            //准备好了
                            Broker broker = new Broker(child);
                            broker.ready();
                            ALL_BROKER.put(child, broker);
                            READY_BROKER.add(broker);
                        }
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                }
            }
            if (READY_BROKER.size() > 0) {
                //已经有准备好的broker
                connectBrokers();
            }
        }
    }

    /**
     * /yxzmq/brokers  下面的children发生了变化,生成完整的主从连接,每一个broker 都只检查自己的subject 是否有变化,其他人的不管
     *
     * @param children
     */
    public void brokerChildrenChange(List<String> children) {
        log.info("brokerChildren change,children is {}", children);
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            ALL_BROKER.clear();
            READY_BROKER.clear();
            for (String child : children) {
                String path = ZkConstant.Path.BROKERS + "/" + child;
                ClientBrokerChildrenWatcher bcw = new ClientBrokerChildrenWatcher(child,this);
                //监视数据
                byte[] data = zk.getData(path, bcw, null);
                if (data != null) {
                    String dataStr = new String(data, Charset.forName("utf-8"));
                    if (StringUtils.equals(ZkConstant.BrokerState.NOT_READY, dataStr)) {
                        //没准备好
                        log.info("{} not ready", path);
                        Broker broker = new Broker(child);
                        ALL_BROKER.put(child, broker);
                    } else if (StringUtils.equals(ZkConstant.BrokerState.READY, dataStr)) {
                        //准备好了
                        log.info("{} ready", path);
                        Broker broker = new Broker(child);
                        broker.ready();
                        ALL_BROKER.put(child, broker);
                        READY_BROKER.add(broker);
                    }
                }
            }
            //TODO 建立主从连接
            connectBrokers();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查已有的brokers  通知client,断开或者新连接的broker
     */
    private synchronized void connectBrokers() {
        List<String> addBrokers = new ArrayList<>();
        List<String> delBrokers = new ArrayList<>();
        List<String> newBrokers = null;
        if (CONNECTED_BROKER.size() == 0) {
            //之前没有连接的broker
            if (READY_BROKER.size() > 0) {
                for (Broker b : READY_BROKER) {
                    addBrokers.add(b.getName());
                }
                newBrokers = listener.addBrokers(addBrokers);
                CONNECTED_BROKER.clear();
                log.info("new brokers is {}", newBrokers);
                if (newBrokers != null) {
                    CONNECTED_BROKER.addAll(getBrokersByNames(newBrokers));
                }
            }
            return;
        }

        for (Broker b : READY_BROKER) {
            if (!CONNECTED_BROKER.contains(b)) {
                addBrokers.add(b.getName());
            }
        }
        for (Broker b : CONNECTED_BROKER) {
            if (!addBrokers.contains(b.getName())) {
                delBrokers.add(b.getName());
            }
        }
        if (!addBrokers.isEmpty()) {
            newBrokers = listener.addBrokers(addBrokers);
        }
        if (!delBrokers.isEmpty()) {
            newBrokers = listener.delBrokers(delBrokers);
        }
        CONNECTED_BROKER.clear();
        log.info("new brokers is {}", newBrokers);
        if (newBrokers != null) {
            CONNECTED_BROKER.addAll(getBrokersByNames(newBrokers));
        }
    }

    /**
     * 根据名字列表获得对象列表
     *
     * @param brokerNames
     * @return
     */
    private static CopyOnWriteArrayList<Broker> getBrokersByNames(List<String> brokerNames) {
        CopyOnWriteArrayList<Broker> result = new CopyOnWriteArrayList<>();
        for (String name : brokerNames) {
            result.add(ALL_BROKER.get(name));
        }
        return result;
    }


    /**
     * broker 状态为ready,添加到ready 列表,重新建立关系
     */
    public void brokerReady(String brokerName) {
        Broker broker = ALL_BROKER.get(brokerName);
        broker.ready();
        READY_BROKER.add(broker);
        connectBrokers();
    }


    /**
     * 删除broker,如果存在于ready 列表,需要重置主从关系
     *
     * @param brokerName
     */
    public void delBroker(String brokerName) {
        ALL_BROKER.remove(brokerName);
        boolean remove = READY_BROKER.remove(brokerName);
        if (remove) {
            connectBrokers();
        }
    }


    public String getMyName() {
        return myName;
    }


    public ClientListener getListener() {
        return listener;
    }

    public void subscribe(String topicName){
        if (StringUtils.isBlank(topicName)){
            throw new NullPointerException();
        }
        String path = ZkConstant.Path.TOPICS + "/" + topicName;
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            Stat state = zk.exists(path, false);
            if (state == null){
                //创建topic节点
                createNode(path);
            }
            //创建订阅节点
            String clientPath = path + "/" + myName;
            createEphemeralNode(clientPath);
        } catch (KeeperException e) {
            KeeperException.Code code = e.code();
            switch (code){
                case SESSIONEXPIRED:
                    //过期
                    restart(ZkServer.getZkVersion());
                    subscribe(topicName);
                    break;

                default:
                    log.warn("subscribe error,code is {}",code);
                    break;
            }
        } catch (InterruptedException e) {
            log.error("subscribe error",e);
        }
    }

    /**
     * 取消订阅
     * @param topicName
     */
    public void cancelSubscribe(String topicName){
        if (StringUtils.isBlank(topicName)){
            throw new NullPointerException();
        }
        String path = ZkConstant.Path.TOPICS + "/" + topicName + "/" + myName;
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            Stat state = zk.exists(path, false);
            if (state == null){
                //没有这个节点,正好
                log.warn("client [{}] cancelSubscribe [{}] but is not subscribing",myName,topicName);
                return;
            }
            //创建订阅节点
            delNode(path);
        } catch (KeeperException e) {
            KeeperException.Code code = e.code();
            switch (code){
                case SESSIONEXPIRED:
                    //过期
                    restart(ZkServer.getZkVersion());
                    cancelSubscribe(topicName);
                    break;
                default:
                    log.warn("subscribe error,code is {}",code);
                    break;
            }
        } catch (InterruptedException e) {
            log.error("subscribe error",e);
        }
    }


    /**
     * 监听
     * @param queueName
     */
    public void listen(String queueName){
        if (StringUtils.isBlank(queueName)){
            throw new NullPointerException();
        }
        String path = ZkConstant.Path.QUEUES + "/" + queueName;
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            Stat state = zk.exists(path, false);
            if (state == null){
                //创建topic节点
                createNode(path);
            }
            //创建订阅节点
            String clientPath = path + "/" + myName;
            createEphemeralNode(clientPath);
        } catch (KeeperException e) {
            KeeperException.Code code = e.code();
            switch (code){
                case SESSIONEXPIRED:
                    //过期
                    restart(ZkServer.getZkVersion());
                    listen(queueName);
                    break;

                default:
                    log.warn("listen error,code is {}",code);
                    break;
            }
        } catch (InterruptedException e) {
            log.error("listen error",e);
        }
    }

    /**
     * 取消监听
     * @param topicName
     */
    public void cancelListen(String queueName){
        if (StringUtils.isBlank(queueName)){
            throw new NullPointerException();
        }
        String path = ZkConstant.Path.QUEUES + "/" + queueName + "/" + myName;
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            Stat state = zk.exists(path, false);
            if (state == null){
                //没有这个节点,正好
                log.warn("client [{}] cancelListen [{}] but is not listen",myName,queueName);
                return;
            }
            //创建订阅节点
            delNode(path);
        } catch (KeeperException e) {
            KeeperException.Code code = e.code();
            switch (code){
                case SESSIONEXPIRED:
                    //过期
                    restart(ZkServer.getZkVersion());
                    cancelListen(queueName);
                    break;
                default:
                    log.warn("cancelListen error,code is {}",code);
                    break;
            }
        } catch (InterruptedException e) {
            log.error("cancelListen error",e);
        }
    }



    /**
     * 创建一个临时节点
     * @param path
     * @throws KeeperException
     * @throws InterruptedException
     */
    private void createEphemeralNode(String path) throws InterruptedException, KeeperException {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            zk.create(path,null,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            KeeperException.Code code = e.code();
            switch (code){
                case NODEEXISTS:
                    //节点已经存在了
                    log.info("create node [{}] ,but node is exist",path);
                    break;
                default:
                    log.warn("createEphemeralNode error,code is {}",code);
                    throw e;
            }
        }
    }

    /**
     * 创建一个永久节点
     * @param path
     * @throws InterruptedException
     */
    private void createNode(String path) throws InterruptedException, KeeperException {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            zk.create(path,null,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            KeeperException.Code code = e.code();
            switch (code){
                case NODEEXISTS:
                    //节点已经存在了
                    log.info("create node [{}] ,but node is exist",path);
                    break;
                default:
                    log.warn("createEphemeralNode error,code is {}",code);
                    throw e;
            }
        }
    }

    private void delNode(String path) throws KeeperException {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            zk.delete(path,0);
        } catch (InterruptedException e) {
            log.error("delNode error",e);
        } catch (KeeperException e) {
            KeeperException.Code code = e.code();
            switch (code){
                case NONODE:
                    //没有这个节点
                    log.warn("delete node [{}] ,but node is not exist",path);
                    break;
                default:
                    log.warn("createEphemeralNode error,code is {}",code);
                    throw e;
            }
        }

    }

}
