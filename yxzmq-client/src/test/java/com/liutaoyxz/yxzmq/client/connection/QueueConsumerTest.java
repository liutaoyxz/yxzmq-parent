package com.liutaoyxz.yxzmq.client.connection;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 13:11 2017/11/23
 * @Description:
 */
public class QueueConsumerTest {

    public static void main(String[] args) throws JMSException {
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession();
        Queue queue = session.createQueue("queue");
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage)message;
                try {
                    String text = textMessage.getText();
                    System.out.println("receive-->"+text);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        });


    }
}
