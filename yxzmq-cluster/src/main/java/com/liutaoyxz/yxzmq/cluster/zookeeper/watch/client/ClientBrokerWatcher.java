package com.liutaoyxz.yxzmq.cluster.zookeeper.watch.client;

import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkClientRoot;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkServer;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 14:18 2017/11/28
 * @Description: /yxzmq/brokers 的watcher 和 callback,处理broker变化的事件,计算broker的 subject 和mirror ,通过listener通知给client
 */
public class ClientBrokerWatcher implements Watcher,AsyncCallback.ChildrenCallback{

    private ZkClientRoot root;

    public static final Logger log = LoggerFactory.getLogger(ClientBrokerWatcher.class);

    public ClientBrokerWatcher(ZkClientRoot root) {
        this.root = root;
    }

    @Override
    public void process(WatchedEvent event) {
        ZooKeeper zk = ZkServer.getZookeeper();
        String path = event.getPath();
        log.debug("watch event,path is {},event is {}",path,event);
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
                //  /yxzmq/brokers  发生变化,异步获取children
                zk.getChildren(path,this,this,path);
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
        KeeperException.Code code = KeeperException.Code.get(rc);
        switch (code){
            case OK:
                // 获得到了children
                root.brokerChildrenChange(children);
                break;
            default:
                log.warn("code not OK, code is {}",code);
                break;
        }
    }
}
