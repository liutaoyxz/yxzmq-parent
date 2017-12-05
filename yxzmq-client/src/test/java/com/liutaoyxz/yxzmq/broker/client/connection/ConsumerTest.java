package com.liutaoyxz.yxzmq.broker.client.connection;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 11:10 2017/11/22
 * @Description:
 */
public class ConsumerTest {

    public static void main(String[] args) throws JMSException {
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession();
        Topic topic = session.createTopic("topic");
        MessageConsumer consumer = session.createConsumer(topic);
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
