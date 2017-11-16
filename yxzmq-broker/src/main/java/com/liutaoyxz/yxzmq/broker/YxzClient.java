package com.liutaoyxz.yxzmq.broker;

import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 10:49 2017/11/15
 * @Description:
 */
public class YxzClient implements Client{

    private static final Logger log = LoggerFactory.getLogger(YxzClient.class);

    private ReadContainer reader = new ReadContainer();

    private String clientId;

    private SocketChannel channel;

    private String address;

    private Lock lock = new ReentrantLock();

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
    public boolean startRead() {
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
                    log.debug("client start read,address is {}",address);
                    return true;
                }
                return false;
            }finally {
                lock.unlock();
            }
        }
        return false;

    }

    @Override
    public List<ProtocolBean> read(ByteBuffer buffer) {
        log.debug("start read thread is {}",Thread.currentThread().getName());
        this.reader.read(buffer);
        return this.reader.flush();
    }

    @Override
    public void stopRead() {
        log.debug("stop read,thread is {}",Thread.currentThread().getName());
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
    public String toString() {
        return "YxzClient{" +
                "clientId='" + clientId + '\'' +
                ", channel=" + channel +
                ", address='" + address + '\'' +
                '}';
    }
}
