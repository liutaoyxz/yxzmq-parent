package com.liutaoyxz.yxzmq.broker.client;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.List;

/**
 * @author Doug Tao
 * @Date: 11:22 2017/12/5
 * @Description: broker 的包装对象
 */
public class BrokerServerClient implements ServerClient{

    private String id;

    private String name;

    private String remoteAddress;

    private int port;

    private NioSocketChannel channel;

    private boolean isBroker;

    /**
     * 是否可用
     */
    private volatile boolean available = false;


    public BrokerServerClient(NioSocketChannel channel) {
        this(channel,false);
    }

    public BrokerServerClient(NioSocketChannel channel,boolean isBroker){
        this.remoteAddress = channel.remoteAddress().getHostName();
        this.port = channel.remoteAddress().getPort();
        this.id = channel.id().toString();
        this.channel = channel;
        this.isBroker = isBroker;
    }

    @Override
    public boolean write(List<byte[]> bytes, boolean sync) {
        return false;
    }

    @Override
    public void read(List<byte[]> bytes, boolean sync) {

    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean isBroker() {
        return this.isBroker;
    }

    @Override
    public String remoteAddress() {
        return null;
    }

    @Override
    public int remotePort() {
        return 0;
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public String toString() {
        return "BrokerServerClient{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", port=" + port +
                ", isBroker=" + isBroker +
                ", available=" + available +
                '}';
    }
}
