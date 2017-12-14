package com.liutaoyxz.yxzmq.cluster.zookeeper;

import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.broker.BrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker.BrokerChildrenWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker.BrokerWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker.QueueWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker.TopicWatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
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
    private static ReentrantLock restartLock = new ReentrantLock();

    /**
     * ready 的broker列表
     */
    private static final CopyOnWriteArrayList<Broker> READY_BROKER = new CopyOnWriteArrayList<>();

    /**
     * 所有的broker  key-broker name
     */
    private static final ConcurrentHashMap<String, Broker> ALL_BROKER = new ConcurrentHashMap<>();

    private static int zkVersion = 1;

    /**
     * 本机的broker对象
     */
    private Broker self;

    private int port;

    private BrokerListener listener;

    private ZkBrokerRoot(int port, BrokerListener listener) {
        this.port = port;
        this.listener = listener;
    }

    private static ZkBrokerRoot root;

    public synchronized static ZkBrokerRoot createRoot(int port, BrokerListener listener, String zookeeperConnectStr) throws IOException {
        if (root == null) {
            ZkServer.createZookeeper(zookeeperConnectStr);
            root = new ZkBrokerRoot(port, listener);
        }
        return root;
    }

    private volatile boolean starting = false;

    public synchronized static ZkBrokerRoot getRoot() {
        return root;
    }


    @Override
    public boolean checkRoot() throws Exception {

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

            log.info("\r brokersStat is {} \r clientsStat is {} \r topicsStat is {} \r queuesStat is {}",
                    brokersStat, clientsStat, topicsStat, queuesStat);
        }
        return true;
    }

    @Override
    public boolean register() {
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            log.info("hostAddress is {}", hostAddress);
            String preBrokerName = hostAddress + ":" + port + "-";
            String createPath = zk.create(ZkConstant.Path.BROKERS + "/" + preBrokerName, ZkConstant.BrokerState.NOT_READY.getBytes(Charset.forName("utf-8")), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            String brokerName = createPath.replace(ZkConstant.Path.BROKERS + "/", "");
            log.info("register broker brokerName is {}", brokerName);
            this.self = new Broker(brokerName);
            root.listener.setMyName(brokerName);
        } catch (UnknownHostException e) {
            log.error("register zookeeper error", e);
        } catch (InterruptedException e) {
            log.error("register zookeeper error", e);
        } catch (KeeperException e) {
            log.error("register zookeeper error", e);
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
            log.error("get brokers error", e);
        }
        return null;
    }

    @Override
    public Broker getBroker() {
        return this.self;
    }

    @Override
    public void start() throws Exception {
        checkRoot();
        ZooKeeper zk = ZkServer.getZookeeper();

        TopicWatcher topicWatch = TopicWatcher.getWatcher();
        QueueWatcher queueWatcher = QueueWatcher.getWatcher();
        BrokerWatcher brokerWatcher = new BrokerWatcher();
        //getChildren 采用同步等待
        //遍历topics
        List<String> topicChildren = zk.getChildren(ZkConstant.Path.TOPICS, topicWatch);
        log.info("topics children {}", topicChildren);
        topicWatch.watchChildren(topicChildren);
        //遍历queues
        List<String> queueChildren = zk.getChildren(ZkConstant.Path.QUEUES, queueWatcher);
        log.info("queues children {}", topicChildren);
        queueWatcher.watchChildren(queueChildren);

        //获取brokers下的 children,第一次获得到children之后遍历children的数据,判断是否为ready,采用同步的方式
        List<String> brokerChildren = zk.getChildren(ZkConstant.Path.BROKERS, brokerWatcher);
        log.info("brokers children {}", brokerChildren);
        createBrokers(brokerChildren);
        register();
        ready();
        this.starting = false;
    }

    /**
     * 连接过期后重启zookeeper 并且重新
     */
    public static void restart(int version) {
        restartLock.lock();
        if (version < zkVersion) {
            return;
        }
        try {
            zkVersion = ZkServer.reCreateZookeeper();
            ZkBrokerRoot.root.start();
        } catch (Exception e) {
            log.error("restart zookeeper error", e);
        } finally {
            restartLock.unlock();
        }
    }


    /**
     * 设置broker的状态为ready,同步
     */
    private void ready() throws KeeperException, InterruptedException {
        ZooKeeper zk = ZkServer.getZookeeper();
        String brokerName = this.self.getName();
        String path = ZkConstant.Path.BROKERS + "/" + brokerName;
        zk.setData(path, ZkConstant.BrokerState.READY.getBytes(Charset.forName("utf-8")), 0);
        log.info("broker ready,broker name is {}", brokerName);
    }


    private void createBrokers(List<String> children) throws InterruptedException {
        ZooKeeper zk = ZkServer.getZookeeper();
        if (children != null) {
            for (String child : children) {
                String path = ZkConstant.Path.BROKERS + "/" + child;
                BrokerChildrenWatcher bcw = new BrokerChildrenWatcher(child);
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
                ZkBrokerRoot.connectBrokers();
            }
        }
    }


    /**
     * /yxzmq/brokers  下面的children发生了变化,生成完整的主从连接,每一个broker 都只检查自己的subject 是否有变化,其他人的不管
     *
     * @param children
     */
    public static void brokerChildrenChange(List<String> children) {
        log.info("brokerChildren change,children is {}", children);
        ZooKeeper zk = ZkServer.getZookeeper();
        try {
            ALL_BROKER.clear();
            READY_BROKER.clear();
            for (String child : children) {
                String path = ZkConstant.Path.BROKERS + "/" + child;
                BrokerChildrenWatcher bcw = new BrokerChildrenWatcher(child);
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
     * 检查broker
     */
    private static void checkBrokers() {


    }

    /**
     * broker 状态为ready,添加到ready 列表,重新建立关系
     */
    public static void brokerStateChange(String brokerName,String newState) {
        Broker broker = ALL_BROKER.get(brokerName);
        if (StringUtils.equals(ZkConstant.BrokerState.READY,newState)){
            //ready
            if (READY_BROKER.contains(broker)){
                return;
            }else {
                broker.ready();
                READY_BROKER.add(broker);
                connectBrokers();
            }
        }else {
            if (READY_BROKER.contains(broker)){
                broker.notReady();
                boolean remove = READY_BROKER.remove(broker);
                if (remove){
                    connectBrokers();
                }
            }
        }

    }


    /**
     * 删除broker,如果存在于ready 列表,需要重置主从关系
     *
     * @param brokerName
     */
    public static void delBroker(String brokerName) {
        ALL_BROKER.remove(brokerName);
        boolean remove = READY_BROKER.remove(brokerName);
        if (remove) {
            connectBrokers();
        }
    }


    /**
     * 建立起来broker 之间的关系,查看自己的subject 是否变化,然后处理
     */
    private static void connectBrokers() {
        log.debug("connectBrokers brokers {}", READY_BROKER);
        Collections.sort(READY_BROKER);
        int size = READY_BROKER.size();
        if (size == 0) {
            return;
        }
        if (size == 1) {
            //todo 通知broker, 就自己,没有subject 和mirror
            log.info("notify broker,no subject and mirror");
            return;
        }
        Broker self = root.self;
        if (self == null) {
            //自己还没准备好
            return;
        }
        String subject = self.getSubject();
        String mirror = self.getMirror();
        String pre = null;
        Broker _self = null;
        String next = null;
        for (Broker b : READY_BROKER) {
            if (_self == null) {
                if (StringUtils.equals(self.getName(), b.getName())) {
                    _self = b;
                    _self.ready();
                    _self.setSubject(pre);
                    continue;
                } else {
                    pre = b.getName();
                    continue;
                }
            }
            if (next == null) {
                next = b.getName();
                _self.setMirror(b.getName());
            }
            break;
        }
        String newSubject = null;
        String newMirror = null;
        if (_self == null) {
            //没准备好 不做任何操作
            return;
        }
        if (_self.getSubject() == null) {
            //最后一个
            newSubject = READY_BROKER.get(size - 1).getName();
            _self.setSubject(newSubject);
        }
        if (_self.getMirror() == null) {
            //最后一个
            newMirror = READY_BROKER.get(0).getName();
            _self.setMirror(newMirror);
        }
        if (subject == null && _self.getSubject() == null) {
            //没变化
        } else if (!StringUtils.equals(self.getSubject(), _self.getSubject())) {
            // subject 改变
            log.debug("notify broker new subject is {}", _self.getSubject());
            root.listener.subjectChange(_self.getSubject());
            self.setSubject(_self.getSubject());
        }
        if (mirror == null && _self.getMirror() == null) {
            //没变化
        } else if (!StringUtils.equals(self.getMirror(), _self.getMirror())) {
            // mirror 改变
            log.info("notify broker new mirror is {}", _self.getMirror());
            ZkBrokerRoot root = ZkBrokerRoot.getRoot();
            root.listener.mirrorChange(_self.getMirror());
            self.setMirror(_self.getMirror());
        }
    }


    public static BrokerListener getListener() {
        return root.listener;
    }

}
