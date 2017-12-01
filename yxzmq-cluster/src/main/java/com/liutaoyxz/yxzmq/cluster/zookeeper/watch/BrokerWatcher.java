package com.liutaoyxz.yxzmq.cluster.zookeeper.watch;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 14:18 2017/11/28
 * @Description: /yxzmq/brokers 的watcher 和 callback,处理broker变化的事件,计算broker的 subject 和mirror ,通过listener通知给需要变动的broker
 */
public class BrokerWatcher implements Watcher,AsyncCallback.ChildrenCallback{

    public static final Logger log = LoggerFactory.getLogger(BrokerWatcher.class);

    @Override
    public void process(WatchedEvent event) {
        ZooKeeper zk = ZkServer.getZookeeper();
        String path = event.getPath();
        log.info("watch event,path is {},event is {}",path,event);
        Event.EventType type = event.getType();
        Event.KeeperState state = event.getState();
        switch (type){
            case None:
                //没有触发任何事件,state 发生了变化,暂时不管
                //重新注册监听

                break;
            case NodeCreated:
                break;

            case NodeDeleted:

                break;
            case NodeChildrenChanged:


                break;

            default:
                log.error("event type error,type is {}",type);
                break;
        }
    }


    /**
     * /yxzmq/brokers 下面的列表发生变化时getChildren 的异步回调
     * @param rc
     * @param path
     * @param ctx
     * @param children
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {

    }
}
