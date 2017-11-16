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
import java.util.concurrent.CountDownLatch;

/**
 * Created by liutao on 2017/11/14.
 */
public class SendDataTest {


    public static void main(String[] args) throws IOException, InterruptedException {

//        Metadata metadata = new Metadata();


//        System.out.println("/127.168.132.155:50434".hashCode());
        testSend();
    }

    public static void testSend() throws IOException, InterruptedException {
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.configureBlocking(false);
//        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.connect(new InetSocketAddress(11171));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i <10 ; i++) {
            Metadata metadata = new Metadata();
            ProtocolBean bean = new ProtocolBean();
            TextMessage msg = new TextMessage("汉字消息 from liutaoyxz ->"+i);

            byte[] msgBytes = ProtostuffUtil.serializable(msg);
            bean.setDataText(msgBytes);



            bean.setDataClass(TextMessage.class.getName());

            byte[] beanBytes = ProtostuffUtil.serializable(bean);
            metadata.setBeanSize(beanBytes.length);

            byte[] mdBytes = ProtostuffUtil.serializable(metadata);
            String ml = ProtostuffUtil.fillMetadataLength(mdBytes.length);
            ByteBuffer byteBuffer = ByteBuffer.wrap(ml.getBytes(Charset.forName("utf-8")));
            socketChannel.write(byteBuffer);
            while (byteBuffer.hasRemaining()){
                socketChannel.write(byteBuffer);
            }

            byteBuffer = ByteBuffer.wrap(mdBytes);
            socketChannel.write(byteBuffer);

            while (byteBuffer.hasRemaining()){
                socketChannel.write(byteBuffer);
            }

            byteBuffer = ByteBuffer.wrap(beanBytes);
            socketChannel.write(byteBuffer);

            while (byteBuffer.hasRemaining()){
                socketChannel.write(byteBuffer);
            }

        }
        countDownLatch.await();



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
