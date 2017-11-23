package com.liutaoyxz.yxzmq.broker.datahandler.analyser;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.messagehandler.YxzMessageHandler;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by liutao on 2017/11/16.
 */
public class DefaultDataAnalyner implements Runnable {

    private Logger log = LoggerFactory.getLogger(DefaultDataAnalyner.class);

    private Client client;

    private ChannelHandler handler;

    public DefaultDataAnalyner(Client client, ChannelHandler handler) {
        this.client = client;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = client.channel();
            ByteBuffer buffer = ByteBuffer.allocate(128);
            int readCount = socketChannel.read(buffer);
            if (readCount == -1) {
                //连接中断
                log.debug("readCount -1,disconnect");
                handler.disconnect(socketChannel);
                return;
            }
            if (readCount == 0) {
                return;
            }
            buffer.flip();
            List<ProtocolBean> beans = client.read(buffer);
            log.debug("beans :{}", beans);
            for (ProtocolBean b : beans) {
                YxzMessageHandler.handlerProtocolBean(b,client);
            }
            return;
        } catch (IOException e) {
            log.debug("channel read error");
            log.debug("channel disconnect by IOException");
            handler.disconnect(client.channel());
//            throw new ConnectCancelException("channel disconnect when read");
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            client.stopRead();
        }
    }
}
