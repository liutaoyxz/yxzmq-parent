package com.liutaoyxz.yxzmq.broker.client.connection;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 11:10 2017/11/22
 * @Description:
 */
public class ProducerTest {


    public static void main(String[] args) throws JMSException, InterruptedException {
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession();
        Topic topic = session.createTopic("topic");
        MessageProducer producer = session.createProducer(topic);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            Thread.sleep(1000L);
            TextMessage textMessage = session.createTextMessage("message-liutao"+i);
            producer.send(textMessage);
        }
        long end = System.currentTimeMillis();
        System.out.println("一共花费了"+(end-start)/1000+"秒");
    }

}
