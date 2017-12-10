package com.liutaoyxz.yxzmq.client.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;

/**
 * @author Doug Tao
 * @Date 下午7:19 2017/12/10
 * @Description:
 */
public class YxzNettyConnectionFactory implements ConnectionFactory {

    private Bootstrap bootstrap;

    private NioEventLoopGroup clientEvent;

    @Override
    public Connection createConnection() throws JMSException {
        return null;
    }

    @Override
    public Connection createConnection(String s, String s1) throws JMSException {
        return null;
    }


    private void startNio(){
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
    }





    @Override
    public JMSContext createContext() {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1) {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1, int i) {
        return null;
    }

    @Override
    public JMSContext createContext(int i) {
        return null;
    }
}
