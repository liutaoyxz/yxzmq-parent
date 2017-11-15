package com.liutaoyxz.yxzmq;

import com.liutaoyxz.yxzmq.io.protocol.*;
import com.liutaoyxz.yxzmq.io.util.ProtostuffUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by liutao on 2017/11/14.
 */
public class SendDataTest {


    public static void main(String[] args) throws IOException {

//        Metadata metadata = new Metadata();


        testSend();
    }

    public static void testSend()  throws IOException{
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.configureBlocking(false);
//        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.connect(new InetSocketAddress(11171));
        Metadata metadata = new Metadata();
        ProtocolBean bean = new ProtocolBean();
        TextMessage msg = new TextMessage("message from liutaoyxz");

        byte[] msgBytes = ProtostuffUtil.serializable(msg);
        bean.setDataText(msgBytes);
        bean.setDataClass(TextMessage.class.getName());
        metadata.setBeanSize(msgBytes.length);
        metadata.setCreateTime(System.currentTimeMillis());
        byte[] mdBytes = ProtostuffUtil.serializable(metadata);
        String ml = ProtostuffUtil.fillMetadataLength(mdBytes.length);
        ByteBuffer byteBuffer = ByteBuffer.allocate(10 + mdBytes.length);
        byteBuffer.put(ml.getBytes("utf-8"));
        byteBuffer.put(mdBytes,10,mdBytes.length);


        socketChannel.write(byteBuffer);

        while (byteBuffer.hasRemaining()){
            socketChannel.write(byteBuffer);
        }



    }

    public static void metadatalengh(){
        Metadata metadata = new Metadata();
        metadata.setCliendId("xxxxxid1");
        metadata.setCreateTime(System.currentTimeMillis());
//        metadata.setIp("127.127.122.1");
        byte[] bytes = ProtostuffUtil.serializable(metadata);

        System.out.println(Arrays.toString(bytes));
        System.out.println(bytes.length);
    }

}
