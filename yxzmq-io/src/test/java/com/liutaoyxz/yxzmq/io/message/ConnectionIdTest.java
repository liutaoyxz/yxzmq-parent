package com.liutaoyxz.yxzmq.io.message;

import com.liutaoyxz.yxzmq.io.message.schema.MessageIdSchema;
import com.liutaoyxz.yxzmq.io.message.schema.Schemas;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * @author Doug Tao
 * @Date: 15:25 2017/12/21
 * @Description:
 */
public class ConnectionIdTest {

    public static final ExecutorService EXECUTOR = new ThreadPoolExecutor(2, 2, 5L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    public static final ExecutorService PROTOSTUFF_EXECUTOR = new ThreadPoolExecutor(1, 100, 5L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>());


    public static final int NUM = 1000;

    public static final CountDownLatch count = new CountDownLatch(2);
    public static final CountDownLatch finish = new CountDownLatch(2);


    @Test
    public void id() throws Exception {
        MessageIdSchema messageIdSchema = new MessageIdSchema();

        ConnectionId cid = new ConnectionId("connectionId");
        SessionId sessionId = new SessionId(cid, "sessionId");
        ProducerId producerId = new ProducerId(sessionId, "producerId");
        MessageId messageId = new MessageId(producerId, "messageId");

        byte[] bytes = ProtobufIOUtil.toByteArray(messageId, messageIdSchema, LinkedBuffer.allocate());
        System.out.println(Arrays.toString(bytes));

        MessageId mergeMessageId = new MessageId();
        ProtobufIOUtil.mergeFrom(bytes, mergeMessageId, messageIdSchema);
        System.out.println("-----------------------protostuff---------------------");
        System.out.println(bytes.length);
        System.out.println(mergeMessageId);
        System.out.println("-----------------------protostuff---------------------");

        System.out.println("-------------------------------java------------------------------------");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(messageId);
        oos.flush();
        byte[] barray = bos.toByteArray();
        System.out.println(Arrays.toString(barray));
        System.out.println(barray.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(barray);
        ObjectInputStream ois = new ObjectInputStream(bis);
        MessageId javMessageId = (MessageId) ois.readObject();
        System.out.println(javMessageId);
        System.out.println("-------------------------------java------------------------------------");

    }

    @Test
    public void testCost() throws Exception {
        this.javaThread();
        this.protostuffThread();
        finish.await();
    }

    private void protostuffThread() throws Exception {
        long start = System.currentTimeMillis();
        CountDownLatch pcount = new CountDownLatch(NUM);
        for (int i = 0; i < NUM; i++) {
            PROTOSTUFF_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        ConnectionId cid = new ConnectionId("connectionId");
                        SessionId sessionId = new SessionId(cid, "sessionId");
                        ProducerId producerId = new ProducerId(sessionId, "producerId");
                        MessageId messageId = new MessageId(producerId, "messageId");

                        byte[] bytes = ProtobufIOUtil.toByteArray(messageId, Schemas.MESSAGE_ID, LinkedBuffer.allocate());
//                    System.out.println(Arrays.toString(bytes));

                        MessageId mergeMessageId = new MessageId();
                        ProtobufIOUtil.mergeFrom(bytes, mergeMessageId, Schemas.MESSAGE_ID);
                        System.out.println("-----------------------protostuff---------------------");
                        System.out.println(bytes.length);
                        System.out.println(mergeMessageId);
                        System.out.println("-----------------------protostuff---------------------");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        pcount.countDown();
                    }

                }
            });
        }
        pcount.await();
        long end = System.currentTimeMillis();
        count.countDown();
        count.await();
        System.out.println("protostuff cost :" + (end - start));
        finish.countDown();
    }

    public void javaThread() throws Exception {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    for (int i = 0; i < NUM; i++) {

                        ConnectionId cid = new ConnectionId("connectionId");
                        SessionId sessionId = new SessionId(cid, "sessionId");
                        ProducerId producerId = new ProducerId(sessionId, "producerId");
                        MessageId messageId = new MessageId(producerId, "messageId");

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(messageId);
                        oos.flush();
                        byte[] barray = bos.toByteArray();
//                        System.out.println(Arrays.toString(barray));
                        System.out.println(barray.length);
                        ByteArrayInputStream bis = new ByteArrayInputStream(barray);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        MessageId javMessageId = (MessageId) ois.readObject();
                        System.out.println(javMessageId);
                        System.out.println("-------------------------------java------------------------------------");


                    }
                    long end = System.currentTimeMillis();
                    count.countDown();
                    count.await();
                    System.out.println("java cost :" + (end - start));
                    finish.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }

}