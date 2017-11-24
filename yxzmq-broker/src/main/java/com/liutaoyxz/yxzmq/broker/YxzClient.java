package com.liutaoyxz.yxzmq.broker;

import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.datahandler.analyser.DefaultDataAnalyner;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 10:49 2017/11/15
 * @Description:
 */
public class YxzClient implements Client{

    public static final AtomicInteger AUTO_INCREASE_CLIENT_ID = new AtomicInteger(1);

    private static final Logger log = LoggerFactory.getLogger(YxzClient.class);

    private ReadContainer reader = new ReadContainer();

    private String clientId;

    private SocketChannel channel;

    private Group parent;

    private String address;

    private Lock lock = new ReentrantLock();

    private ChannelHandler handler;

    private volatile boolean isReading = false;

    private boolean isMainChannel;

    private DefaultDataAnalyner readTask;

    public YxzClient(String clientId, SocketChannel channel, String address,ChannelHandler handler) {
        this.clientId = clientId;
        this.channel = channel;
        this.address = address;
        this.handler = handler;
        this.readTask = new DefaultDataAnalyner(this,this.handler);
    }

    @Override
    public boolean reading() {
        return isReading;
    }

    @Override
    public boolean startRead() {
        if (!check()){
            return false;
        }
        if (!isReading){
            lock.lock();
            try {
                if (!isReading){
                    if (channel == null){
                        throw new NullPointerException("startRead,but channel is null");
                    }
                    if (!channel.isOpen()){
                        log.info("startRead,but channel is not open,address is {}",address);
                    }
                    if (!channel.isConnected()){
                        log.info("startRead,but channel is not connected,address is {}",address);
                    }
                    this.isReading = true;
                    log.debug("client start read,address is {},thread is {},clientId is {}",address,Thread.currentThread().getName(),clientId);
                    return true;
                }
                return false;
            }finally {
                lock.unlock();
            }
        }
        return false;
    }

    private boolean check(){
        if (!channel.isOpen()){
            this.handler.disconnect(channel);
            return false;
        }
        if (!channel.isConnected()){
            this.handler.disconnect(channel);
            return false;
        }
        return true;
    }

    @Override
    public List<ProtocolBean> read(ByteBuffer buffer) {
        log.debug("start read thread is {}",Thread.currentThread().getName());
        this.reader.read(buffer);
        return this.reader.flush();
    }

    @Override
    public void stopRead() {
        log.debug("stop read,address is {},thread is {},clientId is {}",address,Thread.currentThread().getName(),clientId);
        lock.lock();
        try {
            this.isReading = false;
        }finally {
            lock.unlock();
        }
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

    @Override
    public Group parent() {
        return this.parent;
    }

    @Override
    public ChannelHandler handler() {
        return this.handler;
    }

    @Override
    public void setParent(Group group) {
        this.parent = group;
    }

    @Override
    public void setIsMainChannel(boolean isMainChannel) {
        this.isMainChannel = isMainChannel;
    }

    @Override
    public Runnable getDataReadTask() {
        return this.readTask;
    }

    public static String nextClientId(){
        return "broker-client-"+AUTO_INCREASE_CLIENT_ID.getAndIncrement();
    }

    @Override
    public String toString() {
        return "YxzClient{" +
                "clientId='" + clientId + '\'' +
                ", address='" + address + '\'' +
                ", isReading=" + isReading +
                ", isMainChannel=" + isMainChannel +
                ", group=" + parent +
                '}';
    }
}
