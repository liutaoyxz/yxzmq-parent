package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;
import com.liutaoyxz.yxzmq.broker.channelhandler.NettyChannelHandler;
import com.liutaoyxz.yxzmq.broker.channelhandler.NettyClientChannelHandler;
import com.liutaoyxz.yxzmq.broker.client.BrokerServerClient;
import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.broker.client.ServerClientManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author Doug Tao
 * @Date 下午10:21 2017/12/4
 * @Description: 转用netty实现broker 和client
 */
public class NettyServer implements Server,Runnable {

    public static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private ServerConfig config;

    private ServerBootstrap serverBootstrap;

    private Bootstrap bootstrap;

    private CountDownLatch serverStartCountDown = new CountDownLatch(1);

    private ExecutorService serverExecutor = new ThreadPoolExecutor(1, 1, 5L,
            TimeUnit.DAYS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("broker-server-task");
            return thread;
        }
    });

    @Override
    public void start() {
        serverExecutor.execute(this);
        try {
            serverStartCountDown.await();
        } catch (InterruptedException e) {
            log.error("server start error",e);
        }

    }

    @Override
    public void run() {
        if (config == null) {
            config = new ServerConfig();
        }
        EventLoopGroup serverBoos = new NioEventLoopGroup();
        NioEventLoopGroup serverWorker = new NioEventLoopGroup(2);

        NioEventLoopGroup clientEvent = new NioEventLoopGroup();
        ChannelFuture future = null;
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(clientEvent);
            bootstrap.channel(NioSocketChannel.class);
            log.info("broker client started...");
            try {
                //启动server
                this.serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(serverBoos, serverWorker);
                serverBootstrap.channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.config().setAutoRead(true);
                                ch.pipeline().addLast(new ByteArrayDecoder())
                                        .addLast(new ByteArrayEncoder())
                                        .addLast(new NettyChannelHandler());
                            }
                        }).option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                future = serverBootstrap.bind(config.getPort()).sync();
                log.info("server started in port [{}]",config.getPort());
            }finally {
                serverStartCountDown.countDown();
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("start netty server error",e);
        } finally {
            serverBoos.shutdownGracefully();
            serverWorker.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public Server setConfig(ServerConfig config) {
        return this;
    }

    @Override
    public void connect(String host, int port) throws InterruptedException {
        bootstrap.handler(new NettyClientChannelHandler());
        NioSocketChannel channel = (NioSocketChannel) bootstrap.connect(host, port).sync().channel();
        ServerClientManager.addClient(channel);
    }
}
