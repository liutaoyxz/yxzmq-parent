package com.liutaoyxz.yxzmq.broker.datahandler;

import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.util.ProtostuffUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 16:09 2017/11/15
 * @Description:
 */
public abstract class AbstractChannelReader implements ChannelReader {

    private Logger log;

    private static final int headerSize = 10;

    private static final String DEFAULT_CHARSET = "utf-8";

    private ChannelHandler handler;

    private Lock addClientLock = new ReentrantLock();

    protected AbstractChannelReader(Logger log,ChannelHandler handler) {
        this.handler = handler;
        this.log = log;
    }

    // TODO: 2017/11/15 读的线程,先和每个channel对应生成一个线程,实现协议后优化
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void startRead(final Client client) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketChannel socketChannel = client.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(headerSize);
                    // TODO: 2017/11/15 read 时抛出IOException 初步判断为连接已经中断
                    //读取协议头
                    int readCount = socketChannel.read(buffer);
                    if (readCount == 0){
                        return;
                    }
                    if (readCount != headerSize){
                        //协议头 长度不对
                        log.debug("headerSize is not {},clientId is {},read str is {}",headerSize,client.id(),new String(buffer.array(),DEFAULT_CHARSET));

                        return ;
                    }
                    int metaDateSize = Integer.valueOf(new String(buffer.array(),DEFAULT_CHARSET));
                    buffer = ByteBuffer.allocate(metaDateSize);
                    while (buffer.position() < metaDateSize){
                        socketChannel.read(buffer);
                    }
                    buffer.flip();
                    Metadata metadata = ProtostuffUtil.get(buffer.array(), Metadata.class);
                    log.debug("read from client,clientId is {},metadata is {}",client.id(),metadata);

                    buffer.clear();
                    return ;
                } catch (IOException e) {
                    log.error("read from client error",e);
                    handler.disconnect(client.channel());
                }
            }
        });
    }
}
