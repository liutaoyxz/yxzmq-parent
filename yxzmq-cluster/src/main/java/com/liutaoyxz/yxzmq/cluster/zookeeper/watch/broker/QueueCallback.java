package com.liutaoyxz.yxzmq.cluster.zookeeper.watch.broker;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/29
 * @Description: 主题对应的callback,watch 到children发生变化之后采用异步的方式获取列表,回调broker 提供的listener
 */
public class QueueCallback implements AsyncCallback.ChildrenCallback {

    public static final Logger log = LoggerFactory.getLogger(QueueCallback.class);

    /**
     * 队列 和订阅者的映射
     */
    private static final ConcurrentHashMap<String,CopyOnWriteArrayList<String>> QUEUE_SENDERS = new ConcurrentHashMap<>();

    private QueueWatcher watcher;

    public QueueCallback(QueueWatcher watcher) {
        this.watcher = watcher;
    }

    /**
     * /queues  的children 异步callback,对比本地保存的children判断出增加了哪一个主题
     * 完成后继续设置watcher,在watcher中调用异步callback,在这里不再添加callback,否则会出现无限循环
     *
     * @param rc
     * @param path
     * @param ctx
     * @param children
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        KeeperException.Code code = KeeperException.Code.get(rc);
        ZooKeeper zk = ZkServer.getZookeeper();
        switch (code){
            case OK:
                log.info("get queues children OK,path is {},continue watch", path);
                zk.exists(ZkConstant.Path.QUEUES, watcher, null, null);
                //对比本地缓存的queues,通知broker
                if (children != null && !children.isEmpty()){
                    for (String queueName : children){
                        QueueChildrenCallback.watchQueue(queueName);
                    }
                }
                break;
            case CONNECTIONLOSS:
                //连接错误,继续尝试,此时path 是null
                log.warn("get queues children CONNECTIONLOSS,path is {}",path);
                zk.getChildren(ZkConstant.Path.QUEUES, watcher, this, ctx);
                break;

            case NONODE:
                //没有这个节点,停止watch
                log.warn("get queues children NONODE,path is {}",path);
                break;
            default:
                log.warn("callback code not hit,code is {}",code);
                break;
        }
    }


    /**
     * 根据本地保存的children列表,检查出发生变化的queue并且监视这个queue
     * 这里监视到的是 /yxzmq/queues  下面的列表变化,并不是具体某一个主题订阅者的变化
     * @param children
     * @return
     */
    private List<String> checkQueuesChildren(List<String> children){
        Enumeration<String> keys = QUEUE_SENDERS.keys();

        while (keys.hasMoreElements()){
            String queue = keys.nextElement();
            if (children.contains(queue)){
                children.remove(queue);
            }else {
                QUEUE_SENDERS.remove(queue);
                // TODO: 2017/11/30 通知 broker 删除这个主题
                log.info("old queue deleted,queue is {}",queue);
            }
        }
        if (children.size() > 0){
            log.info("there is new queue, new queue is {}",children);
            for (String child : children){
                QueueChildrenCallback.watchQueue(child);
            }
        }

        return null;
    }

    /**
     * 解析某一主题的订阅者,进行分析
     * @param queueName
     * @param listeners
     */
    public static void addListeners(String queueName, List<String> listeners){
        CopyOnWriteArrayList<String> bls = new CopyOnWriteArrayList<>(listeners);
        QUEUE_SENDERS.put(queueName,bls);
        ZkBrokerRoot.getListener().queueListenersChange(queueName,listeners);
    }


}
