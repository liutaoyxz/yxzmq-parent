package com.liutaoyxz.yxzmq.client.connection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date 下午9:47 2017/11/18
 * @Description: 连接容器,保存connection 和 channel的信息
 */
public class ConnectionContainer {

    /**
     * 连接容器,clientID 和 SocketChannel 的映射
     */
    private static final ConcurrentHashMap<String,List<YxzClientChannel>>  csMap = new ConcurrentHashMap<>();

    /**
     * socketChannel 和 YxzClientChannel 的对应关系,方便确认到具体的parent
     */
    private static final ConcurrentHashMap<SocketChannel,YxzClientChannel> syMap = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<YxzDefaultConnection> CONNECTIONS = new CopyOnWriteArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(ConnectionContainer.class);

    private static final AtomicInteger AUTO_INCREASE_ID = new AtomicInteger(1);

    /**
     * 添加锁,添加connections 时需要加锁
     */
    private static final ReentrantLock addLock = new ReentrantLock();


    /**
     * 向容器中添加Connection
     * @param clientID
     * @param channels
     * @return
     */
    static boolean scMap(String clientID, List<YxzClientChannel> channels){
        if (channels == null || channels.isEmpty()){
            log.debug("add connections error,socketChannels is empty.clientID is {}",clientID);
            return false;
        }
        addLock.lock();
        try {
            List<YxzClientChannel> ss = csMap.get(clientID);
            for (YxzClientChannel c: channels){
                syMap.put(c.getChannel(),c);
            }
            if (ss == null){
                csMap.putIfAbsent(clientID,channels);
                return true;
            }
            ss.addAll(channels);
            return true;
        }finally {
            addLock.unlock();
        }
    }




    /**
     * 根据channel获取YxzClientChannel
     * @param channel
     * @return
     */
    static YxzClientChannel getClientChannelBySocketChannel(SocketChannel channel){
        return syMap.get(channel);
    }

    static String createClientID(){
        return "yxzmq-" + AUTO_INCREASE_ID.getAndIncrement();
    }

    static void removeClientChannel(YxzClientChannel channel){
        if (channel == null){
            return;
        }
        if (channel.getChannel() == null){
            syMap.remove(channel);
            return ;
        }

        try {
            SocketChannel sc = channel.getChannel();
            String clientID = channel.getParent().getClientID();
            List<YxzClientChannel> channels = csMap.get(clientID);
            if (channels != null){
                channels.remove(channel);
            }
            syMap.remove(channel);
        } catch (JMSException e) {
            log.debug("removeClientChannel error",e);
        }
    }

    /**
     * connection 保存到列表
     * @param connection
     */
    static void addConnection(YxzDefaultConnection connection){
        CONNECTIONS.add(connection);
    }

}
