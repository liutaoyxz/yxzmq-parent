package com.liutaoyxz.yxzmq.broker.channelhandler;

import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.broker.client.ServerClientManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Doug Tao
 * @Date: 9:05 2017/12/5
 * @Description: 还得是netty  好轮子
 */
public class NettyClientChannelHandler extends SimpleChannelInboundHandler<byte[]> {

    public static final Logger log = LoggerFactory.getLogger(NettyClientChannelHandler.class);


    /**
     * 连接事件,处理连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

    }

    /**
     * 取消连接事件
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        NioSocketChannel channel = (NioSocketChannel) ctx.channel();
        ServerClient client = ServerClientManager.delClient(channel);
    }

    /**
     * 连接激活事件,连接可用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NioSocketChannel channel = (NioSocketChannel) ctx.channel();
        ServerClient client = ServerClientManager.addClient(channel);
        log.info("connect to remote server,server is {}",client);
    }

    /**
     * 读事件,读取到信息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        log.info("channel read,msg is {}", Arrays.toString(bytes));
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
        byte[] bytes =  msg;
        log.info("channel read0,msg is {}", Arrays.toString(bytes));
    }

    /**
     * 读取完成事件
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("channelReadComplete");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("netty error",cause);
    }


}
