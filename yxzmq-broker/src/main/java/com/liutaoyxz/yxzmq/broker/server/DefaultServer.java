package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.ServerConfig;
import com.liutaoyxz.yxzmq.broker.YxzClient;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.datahandler.ChannelReader;
import com.liutaoyxz.yxzmq.broker.datahandler.DefaultChannelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;


/**
 * @author Doug Tao
 * @Date: 9:15 2017/11/15
 * @Description:
 */
public class DefaultServer extends AbstractServer {


    private volatile boolean stopFlag = false;

    private Selector selector;

    private DefaultServer(Logger log, ChannelHandler handler, ChannelReader reader) {
        super(log,handler,reader);
    }

    public DefaultServer(ChannelHandler handler){
//        DefaultChannelHandler defaultChannelHandler = new DefaultChannelHandler();
        this(LoggerFactory.getLogger(DefaultServer.class),handler,new DefaultChannelReader(handler));
    }



    @Override
    public void start() {
        try {

            if (config == null) {
                config = new ServerConfig();
            }
            this.selector = Selector.open();
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(config.getPort()));
            SelectionKey register = channel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("yxzserver started on port {}", config.getPort());
            int ic = 0;
            for (; ; ) {
                if (stopFlag) {
                    break;
                }
                // TODO: 2017/11/15 当开启select() 之后, read 事件是接收到信息才会触发select(), write事件在server端会一直触发,知道切换为止
                ic = selector.select();
                if (ic > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        try {
                            SelectionKey key = keys.next();
//                            key.cancel();
                            handleKey(key);
                        } finally {
                            keys.remove();
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.error("start error", e);
        } finally {
        }

    }

    @Override
    public void stop() {
        this.stopFlag = true;
        System.exit(0);
    }



    /**
     * 处理key
     * @param key
     * @throws IOException
     */
    private void handleKey(SelectionKey key) throws IOException {
        SocketChannel socketChannel = null;
        try {
            if (key.isAcceptable()) {
                socketChannel = ((ServerSocketChannel) key.channel()).accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(128));
                channelHandler.connect(socketChannel);
            }
            if (key.isReadable()) {
                // 读取数据
                socketChannel = (SocketChannel)key.channel();
                Client client = channelHandler.client(socketChannel);
                if (client != null){
                    reader.startRead(client);
//                    socketChannel.register(selector,SelectionKey.OP_WRITE);
//                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    key.interestOps(SelectionKey.OP_READ);
                }

            }
            if (key.isWritable()) {
                // 等待写入状态
                log.debug("key is writable");
                socketChannel = (SocketChannel)key.channel();
//                socketChannel.register(selector,SelectionKey.OP_READ);
//                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (CancelledKeyException e) {
            channelHandler.disconnect(socketChannel);
        } catch (IOException e){
            channelHandler.disconnect(socketChannel);
        }
    }



}
