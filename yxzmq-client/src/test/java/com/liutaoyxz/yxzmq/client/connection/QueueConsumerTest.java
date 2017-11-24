package com.liutaoyxz.yxzmq.client.connection;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Doug Tao
 * @Date: 13:11 2017/11/23
 * @Description:
 */
public class QueueConsumerTest {

    public static void main(String[] args) throws JMSException {
        AtomicInteger count = new AtomicInteger(0);
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession();
        Queue queue = session.createQueue("queue1");
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage)message;
                try {
                    String text = textMessage.getText();
                    System.out.println("receive-->"+text + "  count-->"+count.incrementAndGet());
                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        });


    }
}
