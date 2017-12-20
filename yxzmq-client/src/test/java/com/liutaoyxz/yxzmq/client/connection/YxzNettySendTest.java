package com.liutaoyxz.yxzmq.client.connection;

import org.junit.Test;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Doug Tao
 * @Date: 17:19 2017/12/14
 * @Description:
 */
public class YxzNettySendTest {
    @Test
    public void start() throws Exception {
        YxzNettyConnectionFactory factory = new YxzNettyConnectionFactory("127.0.0.1:2181");
        Connection conn = factory.createConnection();
        conn.start();
    }

    @Test
    public void atomicTest(){
        AtomicBoolean b = new AtomicBoolean(false);
        while (!b.compareAndSet(true,true)){

        }

    }


    public static void main(String[] args) throws Exception{
        YxzNettyConnectionFactory factory = new YxzNettyConnectionFactory("127.0.0.1:2181");
        Connection conn = factory.createConnection();
        conn.start();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("queue4");
        MessageProducer producer = session.createProducer(queue);
        int i = 1;
        for (;;){
            Thread.sleep(500);
            TextMessage message = session.createTextMessage(""+(i++)+"message-message-message-message-message-message-message-message-message" +
                    "-message-message-message-message-message-message-message-message-message-" +
                    "message-message-message-message-message-message-message-message-message-" +
                    "message-message-message-message-message-message-message-message-message-" +
                    "message-message-message-message-message-message-message-message-message-" +
                    "message-message-message-message-message-message-message-");
            producer.send(message);
            System.out.println("send --> "+message.getText());
        }

    }

}