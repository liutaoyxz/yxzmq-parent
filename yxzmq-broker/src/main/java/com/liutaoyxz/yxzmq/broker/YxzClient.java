package com.liutaoyxz.yxzmq.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

/**
 * @author Doug Tao
 * @Date: 10:49 2017/11/15
 * @Description:
 */
public class YxzClient implements Client{

    private static final Logger LOG = LoggerFactory.getLogger(YxzClient.class);

    private String clientId;

    private SocketChannel channel;

    private String address;

    private volatile boolean isReading = false;

    public YxzClient(String clientId, SocketChannel channel, String address) {
        this.clientId = clientId;
        this.channel = channel;
        this.address = address;
    }

    @Override
    public boolean reading() {
        return isReading;
    }

    @Override
    public void startRead() {
        if (channel == null){
            throw new NullPointerException("startRead,but channel is null");
        }
        if (!channel.isOpen()){
            LOG.info("startRead,but channel is not open,address is {}",address);
        }
        if (!channel.isConnected()){
            LOG.info("startRead,but channel is not connected,address is {}",address);
        }
        this.isReading = true;
        LOG.info("client start read,address is {}",address);
    }

    @Override
    public String id() {
        return clientId;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public SocketChannel channel() {
        return channel;
    }
}
