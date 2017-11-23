package com.liutaoyxz.yxzmq.client.connection;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 13:11 2017/11/23
 * @Description:
 */
public class QueueProducerTest {
    public static void main(String[] args) throws JMSException {
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession();
        Queue queue = session.createQueue("queue");
        MessageProducer producer = session.createProducer(queue);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            TextMessage textMessage = session.createTextMessage("message-liutao"+i);
            producer.send(textMessage);
        }
        long end = System.currentTimeMillis();
        System.out.println("一共花费了"+(end-start)/1000+"秒");
    }
}
