package com.liutaoyxz.yxzmq.broker.channelhandler;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.Group;
import com.liutaoyxz.yxzmq.broker.YxzClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Doug Tao
 * @Date: 10:51 2017/11/15
 * @Description: 重新修改连接的管理, 每一个客户端要求创建至少一个主channel 和 一个 辅助channel,
 * 主 channel 主要负责发送queue 和 publish 和 subscribe, 辅助channel 负责 心跳检查,注册主channel
 * 客户端刚连接过来的channel 属于无状态 channel,只有在辅助channel 注册之后才允许发送数据
 *
 */
public abstract class AbstractChannelHandler implements ChannelHandler {

    protected Logger log;

    private Map<SocketChannel,YxzClient> scMap = new ConcurrentHashMap<>();

    /**
     * groupId 和 Group映射
     */
    private ConcurrentHashMap<String,Group> groupMap = new ConcurrentHashMap<>();

    protected AbstractChannelHandler(Logger log) {
        this.log = log;
    }


    @Override
    public boolean connect(SocketChannel channel) {
        check(channel);
        YxzClient client = addClient(channel);
        log.info("client connected,client is {}",client);
        if (client != null){
            afterConnected(client);
            return true;
        }
        return false;
    }

    /**
     * 断开连接,需要清理一些映射关系
     * @s
     * @param channel
     * @return
     */
    @Override
    public boolean disconnect(SocketChannel channel) {
        if (channel == null){
            //连接已经中断
            return true;
        }
        Client client = client(channel);
        if (client == null){
            //连接已经中断
            return true;
        }
        log.info("client disconnected,client is {}", client);
        try {
            removeClient(channel);
            channel.close();
            client.parent().delActiveClient(client);
            if (!client.parent().isAlive()){
                this.groupMap.remove(client.parent().groupId());
            }
            afterDiscontected(client);
        } catch (IOException e) {
            log.debug("disconnect socketChannel error,SocketChannel is {}", channel);
            log.error("disconnect socketChannel error", e);
        }
        return true;
    }


    protected YxzClient addClient(SocketChannel channel){
        check(channel);
        if (!channel.isOpen()){
            log.debug("add client,but channel is not open");
            YxzClient rmClient = scMap.remove(channel);
            return null;
        }
        if (!channel.isConnected()){
            log.debug("add client,but channel is not connected");
            YxzClient rmClient = scMap.remove(channel);
            return null;
        }
        try {
            SocketAddress remoteAddress = channel.getRemoteAddress();
            if (remoteAddress == null){
                log.debug("add client,but remoteAddress is null");
                scMap.remove(channel);
                return null;
            }
            String address = remoteAddress.toString();
            if (StringUtils.isBlank(address)){
                log.debug("add client,but address is blank");
                scMap.remove(channel);
                return null;
            }
            Integer clientId = Math.abs(channel.hashCode());
            YxzClient client = new YxzClient(clientId.toString(),channel,address,this);
            scMap.put(channel,client);
            return client;
        } catch (IOException e) {
            log.error("get remoteAddress error",e);
            scMap.remove(channel);
            return null;
        }
    }

    @Override
    public YxzClient client(SocketChannel channel){
        check(channel);
        return scMap.get(channel);
    }

    /**
     * 连接成功后的一些操作
     * @param client
     */
    protected abstract void afterConnected(Client client);

    /**
     * 取消连接后的一些操作
     * @param client
     */
    protected abstract void afterDiscontected(Client client);

    protected YxzClient removeClient(SocketChannel channel){
        check(channel);
        return scMap.remove(channel);
    }

    protected void check(SocketChannel channel){
        if (channel == null){
            throw new NullPointerException("add client,channel can not be null");
        }
    }

    @Override
    public void addGroup(Group group) {
        this.groupMap.put(group.groupId(),group);
    }

    @Override
    public Group getGroup(String groupId) {
        return this.groupMap.get(groupId);
    }
}
