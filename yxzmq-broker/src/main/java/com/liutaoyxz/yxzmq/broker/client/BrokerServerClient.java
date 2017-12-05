package com.liutaoyxz.yxzmq.broker.client;

import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Doug Tao
 * @Date: 11:22 2017/12/5
 * @Description: broker 的包装对象
 */
public class BrokerServerClient implements ServerClient{

    public static final Logger log = LoggerFactory.getLogger(BrokerServerClient.class);

    private String id;

    private String name;

    private String remoteAddress;

    private int port;

    private NioSocketChannel channel;

    private boolean isBroker;

    private ExecutorService readExecutor;

    private ReadContainer readContainer;

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
        this.readContainer = new ReadContainer();
        this.readExecutor = new ThreadPoolExecutor(1, 1, 5L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("server-client-read-task-"+id);
                return thread;
            }
        });
    }

    @Override
    public boolean write(List<byte[]> bytes, boolean sync) {
        for (byte[] b : bytes){
            channel.writeAndFlush(b);
        }
        return true;
    }

    @Override
    public void read(List<byte[]> bytes, boolean sync) {

    }

    @Override
    public void read(byte[] bytes) {
        this.readContainer.read(bytes);
        List<ProtocolBean> beans = this.readContainer.flush();
        if (beans.size() > 0){
            log.info("read beans :{}",beans);
        }
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String id() {
        return this.id;
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
    public void close() throws IOException {
        if (channel != null && channel.isActive()){
            channel.close();
        }
    }

    @Override
    public void setIsBroker(boolean isBroker) {
        this.isBroker = isBroker;
    }

    @Override
    public void ready() {
        this.available = true;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
