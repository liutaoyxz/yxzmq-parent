package com.liutaoyxz.yxzmq.broker.channelhandler;

import com.liutaoyxz.yxzmq.client.YxzClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Doug Tao
 * @Date: 10:51 2017/11/15
 * @Description:
 */
public abstract class AbstractChannelHandler implements ChannelHandler {

    protected Logger log;

    private Map<SocketChannel,YxzClient> scMap = new ConcurrentHashMap<>();

    protected AbstractChannelHandler(Logger log) {
        this.log = log;
    }

    /**
     * 连接处理
     * @param channel
     * @return
     */
    @Override
    public abstract boolean connect(SocketChannel channel);

    /**
     * 断连处理
     * @param channel
     * @return
     */
    @Override
    public abstract boolean disconnect(SocketChannel channel);

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
                YxzClient rmClient = scMap.remove(channel);
                return null;
            }
            String address = remoteAddress.toString();
            if (StringUtils.isBlank(address)){
                log.debug("add client,but address is blank");
                YxzClient rmClient = scMap.remove(channel);
                return null;
            }
            Integer clientId = address.hashCode();
            YxzClient client = new YxzClient(clientId.toString(),channel,address);
            scMap.put(channel,client);
            return client;
        } catch (IOException e) {
            log.error("get remoteAddress error",e);
            YxzClient rmClient = scMap.remove(channel);
            return null;
        }
    }

    @Override
    public YxzClient client(SocketChannel channel){
        check(channel);
        return scMap.get(channel);
    }

    protected YxzClient removeClient(SocketChannel channel){
        check(channel);
        return scMap.remove(channel);
    }

    protected void check(SocketChannel channel){
        if (channel == null){
            throw new NullPointerException("add client,channel can not be null");
        }
    }

}
