package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;
import com.liutaoyxz.yxzmq.broker.channelhandler.NettyChannelHandler;
import com.liutaoyxz.yxzmq.broker.channelhandler.NettyClientChannelHandler;
import com.liutaoyxz.yxzmq.broker.client.BrokerZkListener;
import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.broker.client.ServerClientManager;
import com.liutaoyxz.yxzmq.cluster.zookeeper.BrokerListener;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkBrokerRoot;
import com.liutaoyxz.yxzmq.io.derby.DerbyTemplate;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
public class NettyServer implements Server, Callable<ChannelFuture> {

    public static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private ServerConfig config;

    private ServerBootstrap serverBootstrap;

    private Bootstrap bootstrap;

    private CountDownLatch serverStartCountDown = new CountDownLatch(1);

    private static NettyServer server;

    private EventLoopGroup serverBoss = new NioEventLoopGroup();
    private NioEventLoopGroup serverWorker = new NioEventLoopGroup(2);

    private NioEventLoopGroup clientEvent = new NioEventLoopGroup();

    private ExecutorService serverExecutor = new ThreadPoolExecutor(1, 1, 5L,
            TimeUnit.DAYS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("broker-server-task");
            return thread;
        }
    });

    private NettyServer() {
    }

    public synchronized static NettyServer getServer() {
        if (server == null) {
            server = new NettyServer();
        }
        return server;
    }

    @Override
    public void start() {
        if (config == null) {
            config = new ServerConfig();
        }
        Future<ChannelFuture> future = serverExecutor.submit(this);
        try {
            ChannelFuture channelFuture = future.get();
            BrokerListener listener = new BrokerZkListener();
            // cluster start
            ZkBrokerRoot root = ZkBrokerRoot.getRoot(config.getPort(), listener);
            root.start();
            // derby start
            DerbyTemplate.createTemplate(config.getDataDir());
            // wait netty stop
            channelFuture.channel().closeFuture().sync();


        } catch (Exception e) {
            log.error("server start error", e);
            System.exit(1);
        } finally {
            serverBoss.shutdownGracefully();
            serverWorker.shutdownGracefully();
            clientEvent.shutdownGracefully();
        }

    }

    @Override
    public ChannelFuture call() {
        ChannelFuture future = null;
        try {
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
                            .addLast(new NettyClientChannelHandler());
                }
            });
            log.info("broker client started...");
            //启动server
            this.serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(serverBoss, serverWorker);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.config().setAutoRead(true);
                            ch.pipeline()
                                    .addLast(new ByteArrayEncoder())
                                    .addLast(new ByteArrayDecoder())
                                    .addLast(new NettyChannelHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            future = serverBootstrap.bind(config.getPort()).sync();
            log.info("server started in port [{}]", config.getPort());
        } catch (InterruptedException e) {
            log.error("start netty server error", e);
        }
        return future;
    }

    @Override
    public void stop() {

    }

    @Override
    public Server setConfig(ServerConfig config) {
        return this;
    }

    @Override
    public ServerClient connect(String host, int port) throws InterruptedException {
        bootstrap.handler(new NettyClientChannelHandler());
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        NioSocketChannel channel = (NioSocketChannel) channelFuture.channel();
//        channelFuture.channel().closeFuture().sync();
        ServerClient client = ServerClientManager.addClient(channel);
        return client;
    }
}
