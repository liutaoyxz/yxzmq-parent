package com.liutaoyxz.yxzmq.io.message;

import com.liutaoyxz.yxzmq.io.message.schema.ConnectionIdSchema;
import com.liutaoyxz.yxzmq.io.message.schema.MessageIdSchema;
import com.liutaoyxz.yxzmq.io.message.schema.ProducerIdSchema;
import com.liutaoyxz.yxzmq.io.message.schema.SessionIdSchema;
import com.liutaoyxz.yxzmq.io.util.TestUser;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 15:25 2017/12/21
 * @Description:
 */
public class ConnectionIdTest {
    @Test
    public void id() throws Exception {
        MessageIdSchema messageIdSchema = new MessageIdSchema();

        ConnectionId cid = new ConnectionId("connectionId");
        SessionId sessionId = new SessionId(cid,"sessionId");
        ProducerId producerId = new ProducerId(sessionId,"producerId");
        MessageId messageId = new MessageId(producerId,"messageId");

        byte[] bytes = ProtobufIOUtil.toByteArray(messageId, messageIdSchema, LinkedBuffer.allocate());
        System.out.println(Arrays.toString(bytes));

        MessageId mergeMessageId = new MessageId();
        ProtobufIOUtil.mergeFrom(bytes,mergeMessageId,messageIdSchema);
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

}