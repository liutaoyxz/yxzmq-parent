package com.liutaoyxz.yxzmq.cluster.zookeeper;

import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.broker.BrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.BrokerWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.QueueWatcher;
import com.liutaoyxz.yxzmq.cluster.zookeeper.watch.TopicWatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ZkBrokerRoot implements BrokerRoot,AsyncCallback.DataCallback,Watcher {


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
     * 所有的broker  key-broker name
     */
    private static final ConcurrentHashMap<String,Broker> ALL_BROKER = new ConcurrentHashMap<>();



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

            //获取brokers下的 children,第一次获得到children之后遍历children的数据,判断是否为ready,采用同步的方式
            List<String> brokerChildren = zk.getChildren(ZkConstant.Path.BROKERS, brokerWatcher);
            log.info("brokers children {}",brokerChildren);
            createBrokers(brokerChildren);
            register();
            ready();
        }catch (KeeperException.ConnectionLossException e){
            log.info("connection loss",e);
        }catch (KeeperException e) {
            log.info("get children error",e);
        }

    }

    /**
     * 设置broker的状态为ready,同步
     */
    private void ready() throws KeeperException, InterruptedException {
        ZooKeeper zk = ZkServer.getZookeeper();
        String brokerName = this.self.getName();
        String path = ZkConstant.Path.BROKERS + "/" + brokerName;
        zk.setData(path,ZkConstant.BrokerState.READY.getBytes(Charset.forName("utf-8")),0);
        log.info("broker ready,broker name is {}",brokerName);
    }


    private void createBrokers(List<String> children) throws InterruptedException {
        ZooKeeper zk = ZkServer.getZookeeper();
        if (children != null){
            for (String child : children){
                String path = ZkConstant.Path.BROKERS + "/" + child;
                try {
                    byte[] data = zk.getData(path, this, null);
                    if (data != null){
                        String dataStr = new String(data,Charset.forName("utf-8"));
                        if (StringUtils.equals(ZkConstant.BrokerState.NOT_READY,dataStr)){
                            //没准备好
                            Broker broker = new Broker(child);
                            ALL_BROKER.put(child,broker);
                        }else if (StringUtils.equals(ZkConstant.BrokerState.READY,dataStr)){
                            //准备好了
                            Broker broker = new Broker(child);
                            broker.ready();
                            ALL_BROKER.put(child,broker);
                            READY_BROKER.add(broker);
                        }
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                }
            }
            if (READY_BROKER.size() > 0){
                //已经有准备好的broker
                Collections.sort(READY_BROKER);
                log.info("sort complete , READY_BROKER is {}",READY_BROKER);
            }
        }
    }


    /**
     * 某一个broker的状态发生变化,处理状态变化
     * @param rc
     * @param path
     * @param ctx
     * @param data
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        String dataStr = new String(data, Charset.forName("utf-8"));
        log.info("path [{}] data is {}",path,dataStr);
        String brokerName = StringUtils.replace(path,ZkConstant.Path.BROKERS+"/","");
        if (StringUtils.equals(ZkConstant.BrokerState.NOT_READY,dataStr)){
            //没准备好
            Broker broker = ALL_BROKER.get(brokerName);
            if (READY_BROKER.contains(broker)){
                //从ready 到 not ready
                boolean remove = READY_BROKER.remove(broker);
                //TODO  查看是否与自己有关,做处理
            }
        }else if (StringUtils.equals(ZkConstant.BrokerState.READY,dataStr)){
            //准备好了
            Broker broker = ALL_BROKER.get(brokerName);
            READY_BROKER.add(broker);
            Collections.sort(READY_BROKER);
            //TODO 新增加了一个broker,做相关的处理
            log.info("new broker ready,brokerName is {}",brokerName);
        }

    }

    /**
     * 监视某一个broker
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        Event.KeeperState state = event.getState();
        Event.EventType type = event.getType();
        ZooKeeper zk = ZkServer.getZookeeper();
        switch (type){
            case NodeDataChanged:
                //data 变化,可能是ready变成 notready 或者相反
                log.info("path [{}] data change",path);
                zk.getData(path,this,this,path);
                break;
            case NodeDeleted:
                // broker 关闭了,删除这个broker
                break;
            case None:
                //TODO state 发生变化,暂不处理
                break;
            default:
                break;
        }
    }

    /**
     * /yxzmq/brokers  下面的children发生了变化
     * @param children
     */
    public static void brokerChildrenChange(List<String> children){
        log.info("brokerChildren change,children is {}",children);

    }


    /**
     * 检查broker
     */
    private static void checkBrokers(){



    }


}
