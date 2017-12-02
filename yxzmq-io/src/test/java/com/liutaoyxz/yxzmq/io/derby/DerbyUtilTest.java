package com.liutaoyxz.yxzmq.io.derby;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;

import java.sql.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Doug Tao
 * @Date 下午11:09 2017/12/1
 * @Description:
 */
public class DerbyUtilTest {


    @Test
    public void derbyConnTest(){



    }



    @Test
    public void derbyInsertTest(){
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String url = "jdbc:derby:/Users/liutao/db/derby;create=true";
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url);
            Statement statement = conn.createStatement();
//            String createSchema = "create schema yxzmq";
//            statement.execute(createSchema);

            String setSchema = "set schema yxzmq";

            statement.execute(setSchema);

            String insert = "insert into yxzmq.queue (id,name,broker_name) values (5,'queue2','123')";

            statement.execute(insert);

            String select = "select * from queue";

            boolean execute = statement.execute(select);

            ResultSet resultSet = statement.getResultSet();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();


            while (resultSet.next()){
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println(resultSet.getString(i));
                }

            }


            CountDownLatch latch = new CountDownLatch(1);
            latch.await();
        }catch(Exception e) {
            e.printStackTrace();
        }finally {

        }
    }

    @Test
    public void propertyTest(){
        System.out.println(System.getProperty("user.home"));
    }

    @Test
    public void poolTest() throws Exception {
        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String url = "jdbc:derby:/Users/liutao/db/derby;create=true";
        DerbyPoolFactory factory = new DerbyPoolFactory(url,"yxzmq");
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(10);
        config.setMaxTotal(10);
        config.setMinIdle(0);
        GenericObjectPool<Connection> pool = new GenericObjectPool<>(factory,config);
        for (int j = 0; j < 20; j++) {
            Connection conn = pool.borrowObject();
            try {
                String selectSql = "select * from queue";
                Statement sm = conn.createStatement();
                boolean execute = sm.execute(selectSql);
                System.out.println("execute result :"+execute);
                ResultSet resultSet = sm.getResultSet();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount ; i++) {
                    System.out.print(metaData.getColumnName(i) + "\t");
                }
                System.out.println();
                while (resultSet.next()){
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(resultSet.getString(i)+"\t");
                    }
                    System.out.println();
                }
                System.out.println("----------------"+j+"--------------");
            }finally {
                pool.returnObject(conn);
            }

        }
    }

    @Test
    public void templeteTest() throws Exception {
        DerbyTemplate templete = DerbyTemplate.createTemplate("/Users/liutao/yxzmqData");
        templete.test();

    }

    @Test
    public void insertTest() throws Exception{
        DerbyTemplate template = DerbyTemplate.createTemplate("/Users/liutao/yxzmqData");
        AtomicInteger queueId = new AtomicInteger(1);
        String brokerName = "127.0.0.1:11171";
        for (int i = 0; i < 1000; i++) {

            QueueModal queue = new QueueModal();
            queue.setQueueId(queueId.getAndIncrement() + "");
            queue.setBrokerName(brokerName);
            queue.setMessage("message"+(i+1));
            queue.setQueueName("q1");
            template.insertQueue(queue);
        }


        List<QueueModal> list = template.selectByBrokerName(brokerName);
        System.out.println(list);

        template.deleteByBrokerName(brokerName);

        List<QueueModal> list2 = template.selectByBrokerName(brokerName);
        System.out.println(list2);

    }



}