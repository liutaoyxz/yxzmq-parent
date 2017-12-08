package com.liutaoyxz.yxzmq.cluster.zookeeper.callback;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkConstant;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author Doug Tao
 * @Date: 14:39 2017/12/1
 * @Description:
 */
public class BrokerChildrenWatcher implements AsyncCallback.DataCallback,Watcher {

    public static final Logger log = LoggerFactory.getLogger(BrokerChildrenWatcher.class);

    /**
     * brokerName
     */
    private String brokerName;

    public BrokerChildrenWatcher(String brokerName) {
        this.brokerName = brokerName;
    }

    /**
     * 数据改变的监视器
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        String path = ZkConstant.Path.BROKERS + "/" + brokerName;
        Event.KeeperState state = event.getState();
        Event.EventType type = event.getType();
        ZooKeeper zk = ZkServer.getZookeeper();
        switch (type){
            case None:
                //连接状态发生变化
                switch (state){
                    case Expired:
                        //连接过期,重新连接
                        log.info("zookeeper expired,restart zookeeper");
                        ZkBrokerRoot.restart(ZkServer.getZkVersion());
                        break;
                    default:
                        log.debug("path  [{}]  data change , notify broker ",path);
                        zk.getData(path,this,this,path);
                        break;
                }
                break;
            case NodeDataChanged:
                //broker 数据发生变化,获取状态并继续监视
                log.debug("path  [{}]  data change , notify broker ",path);
                zk.getData(path,this,this,path);
                break;

            case NodeDeleted:
                log.info("brokerName [{}] is deleted",brokerName);
                ZkBrokerRoot.delBroker(brokerName);
                break;
            default:
                log.warn("broker [{}] data change, state is {},type is {}",brokerName,state,type);
                zk.getData(path,this,this,path);
                break;
        }


    }

    /**
     * 数据发生改变后,获取数据的回调
     * @param rc
     * @param path
     * @param ctx
     * @param data
     * @param stat
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        KeeperException.Code code = KeeperException.Code.get(rc);
        String brokerName = path.replace(ZkConstant.Path.BROKERS + "/","");
        switch (code){
            case OK:
                //正常 todo 获取数据,然后通过listener 通知broker
                String brokerData = new String(data, Charset.forName("utf-8"));

                ZkBrokerRoot.brokerReady(brokerName);
                log.info("broker [{}] data change , new date is {}",brokerName,brokerData);
                break;
            case CONNECTIONLOSS:
                //连接丢失 todo


                break;
            case NONODE:
                // 没有这个节点了,删除他
                log.info("broker [{}] not exist",brokerName);
                ZkBrokerRoot.delBroker(brokerName);
                break;
            default:
                log.warn("path [{}] get broker data,code is {} ",path,code);
                break;
        }
    }
}
