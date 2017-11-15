package com.liutaoyxz.yxzmq.broker.channelhandler;

import com.liutaoyxz.yxzmq.broker.YxzClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author Doug Tao
 * @Date: 9:46 2017/11/15
 * @Description:
 */
public class DefaultChannelHandler extends AbstractChannelHandler {

    private DefaultChannelHandler(Logger log) {
        super(log);
    }

    public DefaultChannelHandler() {
        this(LoggerFactory.getLogger(DefaultChannelHandler.class));
    }

    @Override
    public boolean connect(SocketChannel channel) {
        check(channel);
        YxzClient client = addClient(channel);
        log.info("client connected,client is {}",client);
        return true;
    }

    @Override
    public boolean disconnect(SocketChannel channel) {
        check(channel);
        YxzClient client = client(channel);
        String remoteAddress = client.address();
        log.info("client disconnected,client is {}", client);
        try {
            removeClient(channel);
            channel.close();
        } catch (IOException e) {
            log.debug("disconnect socketChannel error,SocketChannel is {}", channel);
            log.error("disconnect socketChannel error", e);
        }
        return true;
    }


}
