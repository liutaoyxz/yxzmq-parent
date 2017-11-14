package com.liutaoyxz.yxzmq;

import com.liutaoyxz.yxzmq.client.protocol.DefaultStringData;
import com.liutaoyxz.yxzmq.client.protocol.Metadata;
import com.liutaoyxz.yxzmq.client.protocol.TestCommand;
import com.liutaoyxz.yxzmq.client.util.ProtostuffUtil;
import io.protostuff.ProtostuffIOUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by liutao on 2017/11/14.
 */
public class SendDataTest {


    public static void main(String[] args) throws IOException {

        Metadata metadata = new Metadata();
        metadata.setCreateTime(System.currentTimeMillis());
        metadata.setCliendId("xxxxxidhid22");
        DefaultStringData data = new DefaultStringData();
        data.setText("thisisdata");
        TestCommand command = new TestCommand();
        byte[] dataBytes = ProtostuffUtil.serializable(data);
        byte[] commondBytes = ProtostuffUtil.serializable(command);
        metadata.setDataLenght(dataBytes.length);
        metadata.setCommandLenght(commondBytes.length);
        byte[] metadataBytes = ProtostuffUtil.serializable(metadata);
        System.out.println(Arrays.toString(metadataBytes));
        String msl = ProtostuffUtil.fillMetadataLength(metadataBytes.length);
        System.out.println(msl);

        testSend();
    }

    public static void testSend()  throws IOException{
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.configureBlocking(false);
//        socketChannel.register(selector, SelectionKey.OP_READ);
        socketChannel.connect(new InetSocketAddress(11171));
        String msg = "client msg ->";
        for (int i = 0; i < 1000; i++) {
            msg += "client msg -> "+i;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes(Charset.forName("utf-8")));
        while (byteBuffer.hasRemaining()){
            socketChannel.write(byteBuffer);
        }
        System.out.println("msg size -> "+msg.length() );

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
