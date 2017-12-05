package com.liutaoyxz.yxzmq.broker.client.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Doug Tao
 * @Date 下午10:39 2017/11/20
 * @Description: 工厂任务,执行selector
 */
public class FactoryTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(FactoryTask.class);

    private Selector selector;

    private  AtomicBoolean isStart = new AtomicBoolean(false);

    private AtomicBoolean stop = new AtomicBoolean(false);

    public FactoryTask(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            int ic = 0;
            while (!stop.get()) {
                // TODO: 2017/11/15 当开启select() 之后, read 事件是接收到信息才会触发select(), write事件在server端会一直触发,知道切换为止
                ic = selector.select();
                isStart.set(true);
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
            isStart.set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key){
        SocketChannel socketChannel = null;
        YxzClientChannel cc = null;
        try {
            if (key.isAcceptable()) {

            }
            if (key.isConnectable()){
                log.debug("client is connectable");
                socketChannel =(SocketChannel) key.channel();
                if (socketChannel.finishConnect()){
                    log.debug("connect finish");
                    socketChannel.register(selector,SelectionKey.OP_READ);
                }else {
                    log.debug("connect error");
                }
            }
            if (key.isReadable()) {
                // 读取数据
                socketChannel = (SocketChannel)key.channel();
                cc = ConnectionContainer.getClientChannelBySocketChannel(socketChannel);
                ByteBuffer buffer = ByteBuffer.allocate(128);
                int readCount = socketChannel.read(buffer);
                if (readCount == -1) {
                    //连接中断
                    log.debug("readCount -1,disconnect");
                    cc.getParent().disconnected(cc);
                    return;
                }
                if (readCount == 0) {
                    return;
                }
                buffer.flip();
                if (cc.startRead()){
                    cc.read(buffer);
                }
            }
            if (key.isWritable()) {
                // 等待写入状态
                log.debug("key is writable");
                socketChannel = (SocketChannel)key.channel();
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (CancelledKeyException e) {
            log.debug("channel disconnect by CancelledKeyException");
            if (cc != null){
                try {
                    cc.getParent().disconnected(cc);
                } catch (IOException e1) {
                    log.debug("disconnect error",e);
                }
            }
        }catch (IOException e){
            if (cc != null){
                try {
                    cc.getParent().disconnected(cc);
                } catch (IOException e1) {
                    log.debug("disconnect error",e);
                }
            }
        }
    }

    public void start(){
        this.isStart.compareAndSet(true,true);
    }

    public void stop(){
        stop.set(true);
        selector.wakeup();
        this.isStart.compareAndSet(false,false);
    }

    public static void main(String[] args) {

    }

}
