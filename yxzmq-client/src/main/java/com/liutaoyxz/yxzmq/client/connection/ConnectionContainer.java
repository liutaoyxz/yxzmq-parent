package com.liutaoyxz.yxzmq.client.connection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final ConcurrentHashMap<String,List<SocketChannel>>  csMap = new ConcurrentHashMap<>();

    private static final CopyOnWriteArrayList<YxzDefaultConnection> CONNECTIONS = new CopyOnWriteArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(ConnectionContainer.class);

    private static final AtomicInteger AUTO_INCREASE_ID = new AtomicInteger(1);

    /**
     * 添加锁,添加connections 时需要加锁
     */
    private static final ReentrantLock addLock = new ReentrantLock();

    /**
     * 获取channels
     * @param clientID clientID 与broker端的 clientId 不是一个
     * @return
     */
    static List<SocketChannel> getChannels(String clientID){
        if (StringUtils.isBlank(clientID)){
            log.debug("clientID is blank");
            return null;
        }
        return csMap.get(clientID);
    }

    /**
     * 向容器中添加Connection
     * @param clientID
     * @param socketChannels
     * @return
     */
    static boolean scMap(String clientID, List<SocketChannel> socketChannels){
        if (socketChannels == null || socketChannels.isEmpty()){
            log.debug("add connections error,socketChannels is empty.clientID is {}",clientID);
            return false;
        }
        addLock.lock();
        try {
            List<SocketChannel> ss = csMap.get(clientID);
            if (ss == null){
                csMap.putIfAbsent(clientID,socketChannels);
                return true;
            }
            ss.addAll(socketChannels);
            return true;
        }finally {
            addLock.unlock();
        }
    }

    static boolean connect(String clientID, InetSocketAddress address) throws IOException{
        addLock.lock();
        try {
            List<SocketChannel> ss = csMap.get(clientID);
            if (ss == null){
                log.debug("connection has no socketChannel,clientID is {}",clientID);
                return false;
            }
            for (SocketChannel channel : ss){
                channel.connect(address);
            }
        }finally {
            addLock.unlock();
        }
        return true;
    }

    static String createClientID(){
        return "yxzmq-" + AUTO_INCREASE_ID.getAndIncrement();
    }

    /**
     * connection 保存到列表
     * @param connection
     */
    static void addConnection(YxzDefaultConnection connection){
        CONNECTIONS.add(connection);
    }

}
