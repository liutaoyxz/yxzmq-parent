package com.liutaoyxz.yxzmq.client.connection;

import org.junit.Test;

import javax.jms.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Doug Tao
 * @Date 上午1:03 2017/11/19
 * @Description:
 */
public class YxzDefaultConnectionFactoryTest {
    @Test
    public void createConnection() throws Exception {
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        System.out.println("ok");

    }


    @Test
    public void threadTest(){
        ScheduledExecutorService se = Executors.newScheduledThreadPool(1);
        se.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("thread start");
            }
        },0L, TimeUnit.SECONDS);

        System.out.println("end");
    }

    public static void main(String[] args) throws JMSException {
        YxzDefaultConnectionFactory factory = YxzDefaultConnectionFactory.getFactory();
        Connection connection = factory.createConnection();
        connection.start();
//        Session session = connection.createSession();
//        Topic topic = session.createTopic("topic");
//        MessageProducer producer = session.createProducer(topic);
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            TextMessage textMessage = session.createTextMessage("message-liutao"+i);
//            producer.send(textMessage);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println("一共花费了"+(end-start)/1000+"秒");

    }
}