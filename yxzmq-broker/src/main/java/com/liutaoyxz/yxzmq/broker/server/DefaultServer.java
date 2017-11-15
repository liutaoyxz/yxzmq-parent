package com.liutaoyxz.yxzmq.broker.server;

import com.liutaoyxz.yxzmq.broker.ServerConfig;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.channelhandler.DefaultChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;


/**
 * @author Doug Tao
 * @Date: 9:15 2017/11/15
 * @Description:
 */
public class DefaultServer extends AbstractServer {


    private volatile boolean stopFlag = false;

    private Selector selector;

    private DefaultServer(Logger log, ChannelHandler handler) {
        super(log,handler);
    }

    public DefaultServer(){
        this(LoggerFactory.getLogger(DefaultServer.class),new DefaultChannelHandler());
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
                ic = selector.select();
                if (ic > 0) {
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        try {
                            SelectionKey key = keys.next();
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
                socketChannel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(10);
                readLoop:
                while (true) {
                    // TODO: 2017/11/15 read 时抛出IOException 初步判断为连接已经中断
                    int readCount = socketChannel.read(buffer);
                    if (readCount == -1) {
                        // 连接已经关闭
                        socketChannel.close();
                        break readLoop;
                    }
                    if (readCount == 0) {
                        //没有内容
                        break readLoop;
                    }
                    buffer.flip();
                    String msg = Charset.forName(DEFAULT_CHARSET).decode(buffer).toString();
                    log.info("receive message : {}", msg);
                    if (msg != null && msg.equals("stop")){
                        log.info("server stop");
                        stop();
                    }
                    buffer.clear();
                }
                key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            if (key.isWritable()) {
                // 等待写入状态
            }
        } catch (CancelledKeyException e) {
            channelHandler.disconnect(socketChannel);
        } catch (IOException e){
            channelHandler.disconnect(socketChannel);
        }
    }



}
