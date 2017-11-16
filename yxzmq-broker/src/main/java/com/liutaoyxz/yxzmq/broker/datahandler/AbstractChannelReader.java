package com.liutaoyxz.yxzmq.broker.datahandler;

import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.io.protocol.Message;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.util.ProtostuffUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.liutaoyxz.yxzmq.io.protocol.ReadContainer.METADATA_SIZE_LENGTH;

import static com.liutaoyxz.yxzmq.io.protocol.ReadContainer.DEFAULT_CHARSET;

/**
 * @author Doug Tao
 * @Date: 16:09 2017/11/15
 * @Description:
 */
public abstract class AbstractChannelReader implements ChannelReader {

    private Logger log;

    private ChannelHandler handler;

    private Lock addClientLock = new ReentrantLock();

    private AtomicInteger incrId = new AtomicInteger(1);

    protected AbstractChannelReader(Logger log,ChannelHandler handler) {
        this.handler = handler;
        this.log = log;
    }

    // TODO: 2017/11/15 读的线程,先和每个channel对应生成一个线程,实现协议后优化
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void startRead(final Client client) {
        if (!client.startRead()){
            return ;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setName(client.id()+"-"+incrId.getAndIncrement());
                    SocketChannel socketChannel = client.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(128);

                    // TODO: 2017/11/15 read 时抛出IOException 初步判断为连接已经中断
                    //读取协议头
                    int readCount = socketChannel.read(buffer);
                    if (readCount == -1){
                        //连接中断
                        handler.disconnect(socketChannel);
                        return;
                    }
                    if (readCount == 0){
                        return;
                    }
                    buffer.flip();
                    List<ProtocolBean> beans = client.read(buffer);
                    log.debug("beans :{}",beans);
                    for (ProtocolBean b : beans){
                        Message o = null;
                        try {
                            o = (Message) ProtostuffUtil.get(b.getDataText(), Class.forName(b.getDataClass()));
                            System.out.println(o);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        System.out.println(o.getContent());
                    }
                    return ;
                } catch (IOException e) {
//                    log.error("read from client error",e);
                    handler.disconnect(client.channel());
                }finally {
                    client.stopRead();
                }
            }
        });
    }
}
