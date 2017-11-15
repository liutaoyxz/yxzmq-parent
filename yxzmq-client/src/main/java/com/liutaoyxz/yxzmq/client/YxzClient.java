package com.liutaoyxz.yxzmq.client;

import java.nio.channels.SocketChannel;

/**
 * @author Doug Tao
 * @Date: 10:49 2017/11/15
 * @Description:
 */
public class YxzClient {
    private String clientId;

    private SocketChannel channel;

    private String address;


    public YxzClient(String clientId, SocketChannel channel, String address) {
        this.clientId = clientId;
        this.channel = channel;
        this.address = address;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "YxzClient{" +
                "clientId='" + clientId + '\'' +
                ", channel=" + channel +
                ", address='" + address + '\'' +
                '}';
    }
}
