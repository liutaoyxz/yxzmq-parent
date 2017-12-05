package com.liutaoyxz.yxzmq.broker.client.connection;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 13:11 2017/11/23
 * @Description:
 */
public class QueueProducerTest {
    public static void main(String[] args) throws JMSException, InterruptedException {
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        connection.start();
        Session session = connection.createSession();
        Queue queue = session.createQueue("queue1");
        MessageProducer producer = session.createProducer(queue);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
//            Thread.sleep(500L);
            TextMessage textMessage = session.createTextMessage("message-liutao"+i);
            producer.send(textMessage);
            System.out.println("send msg->"+textMessage.getText());
        }
        long end = System.currentTimeMillis();
        System.out.println("一共花费了"+(end-start)/1000+"秒");
    }
}
