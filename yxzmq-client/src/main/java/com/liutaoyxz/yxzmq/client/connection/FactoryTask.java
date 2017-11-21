package com.liutaoyxz.yxzmq.client.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author Doug Tao
 * @Date 下午10:39 2017/11/20
 * @Description: 工厂任务,执行selector
 */
public class FactoryTask implements Runnable {

    private final Logger log = LoggerFactory.getLogger(FactoryTask.class);

    private Selector selector;

    public FactoryTask(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            int ic = 0;
            for (; ; ) {
                // TODO: 2017/11/15 当开启select() 之后, read 事件是接收到信息才会触发select(), write事件在server端会一直触发,知道切换为止
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
            e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) throws IOException{
        SocketChannel socketChannel = null;
        try {
            if (key.isAcceptable()) {

            }
            if (key.isReadable()) {
                // 读取数据
                socketChannel = (SocketChannel)key.channel();
//                socketChannel.read()1
                YxzClientChannel cc = ConnectionContainer.getClientChannelBySocketChannel(socketChannel);


            }
            if (key.isWritable()) {
                // 等待写入状态
                log.debug("key is writable");
                socketChannel = (SocketChannel)key.channel();
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (CancelledKeyException e) {
            log.debug("channel disconnect by CancelledKeyException");
//            handler.disconnect(socketChannel);
        }
    }

}
