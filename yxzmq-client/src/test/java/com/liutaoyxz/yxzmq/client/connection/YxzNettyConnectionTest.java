package com.liutaoyxz.yxzmq.client.connection;

import org.junit.Test;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Doug Tao
 * @Date: 17:19 2017/12/14
 * @Description:
 */
public class YxzNettyConnectionTest {
    @Test
    public void start() throws Exception {
        YxzNettyConnectionFactory factory = new YxzNettyConnectionFactory("127.0.0.1:2181");
        Connection conn = factory.createConnection();
        conn.start();
    }

    @Test
    public void atomicTest() {
        AtomicBoolean b = new AtomicBoolean(false);
        while (!b.compareAndSet(true, true)) {

        }

    }


    public static void main(String[] args) throws Exception {
        YxzNettyConnectionFactory factory = new YxzNettyConnectionFactory("127.0.0.1:2181");
        Connection conn = factory.createConnection();
        conn.start();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("queue1");
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage msg = (TextMessage) message;
                try {
                    System.out.println(msg.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
//        Thread.sleep(30000);
//        session.close();

    }

}