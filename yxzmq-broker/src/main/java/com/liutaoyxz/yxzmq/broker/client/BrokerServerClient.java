package com.liutaoyxz.yxzmq.broker.client;

import com.liutaoyxz.yxzmq.broker.messagehandler.NettyMessageHandler;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
public class BrokerServerClient implements ServerClient {

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
        this(channel, false);
    }

    public BrokerServerClient(NioSocketChannel channel, boolean isBroker) {
        this.remoteAddress = channel.remoteAddress().getAddress().getHostAddress();
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
                thread.setName("server-client-read-task-" + id);
                return thread;
            }
        });
    }

    @Override
    public boolean write(List<byte[]> bytes, boolean sync) throws InterruptedException {
        int length = 0;
        for (byte[] b : bytes) {
            length += b.length;
        }
        ByteBuf buf = channel.alloc().buffer(length);
        for (byte[] b : bytes) {
            buf.writeBytes(b);
        }
        ChannelFuture future = channel.writeAndFlush(buf);
        if (sync) {
            future.sync();
        }
        log.info("future {}", future);
        return true;
    }

    @Override
    public void read(List<byte[]> bytes, boolean sync) {

    }

    @Override
    public void read(byte[] bytes) throws IOException {
        this.readContainer.read(bytes);
        List<ProtocolBean> beans = this.readContainer.flush();
        log.info("read beans :{}", beans);
        for (ProtocolBean b : beans){
            NettyMessageHandler.handlerProtocolBean(b,this);
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
        return this.remoteAddress;
    }

    @Override
    public int remotePort() {
        return this.port;
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public void close() throws IOException {
        if (channel != null && channel.isActive()) {
            this.available = false;
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

    class ReadTask implements Runnable{

        @Override
        public void run() {

        }
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
