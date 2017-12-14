package com.liutaoyxz.yxzmq.client.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Doug Tao
 * @Date 下午7:19 2017/12/10
 * @Description:
 */
public class YxzNettyConnectionFactory implements ConnectionFactory {

    private static final Logger log = LoggerFactory.getLogger(YxzNettyConnectionFactory.class);

    private Bootstrap bootstrap;

    private NioEventLoopGroup clientEvent = new NioEventLoopGroup();

    private String zookeeperStr;

    /**
     * 定时任务,心跳测试,暂时没用,只是保证程序不退出
     */
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("scheduled-task");
            return thread;
        }
    });

    public YxzNettyConnectionFactory(String zookeeperStr) {
        this.zookeeperStr = zookeeperStr;
        startNio();
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                log.debug("factory started");
            }
        }, 0L, TimeUnit.MILLISECONDS);
    }

    /**
     * 建立一个到broker 的连接
     * @param ip
     * @param port
     * @return
     * @throws InterruptedException
     */
    public NioSocketChannel connectBroker(String ip,int port,YxzNettyConnection conn) throws InterruptedException {
        bootstrap.handler(new NettyClientChannelHandler(conn));
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        NioSocketChannel channel = (NioSocketChannel) channelFuture.channel();
        return channel;
    }

    @Override
    public Connection createConnection() throws JMSException {
        return new YxzNettyConnection(this.zookeeperStr,this);
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        return null;
    }


    private void startNio() {
        bootstrap = new Bootstrap();
        bootstrap.group(clientEvent);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new ByteArrayEncoder())
                        .addLast(new ByteArrayDecoder())
                        ;
            }
        });
    }


    @Override
    public JMSContext createContext() {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1) {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1, int i) {
        return null;
    }

    @Override
    public JMSContext createContext(int i) {
        return null;
    }
}
