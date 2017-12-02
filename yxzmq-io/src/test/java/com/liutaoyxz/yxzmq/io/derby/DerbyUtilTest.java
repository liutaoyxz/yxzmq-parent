package com.liutaoyxz.yxzmq.io.derby;

import org.junit.Test;

import java.sql.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author Doug Tao
 * @Date 下午11:09 2017/12/1
 * @Description:
 */
public class DerbyUtilTest {


    @Test
    public void derbyConnTest(){
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
            String createTable = "CREATE TABLE queue " +
                    "(" +
                    "  ID int PRIMARY KEY NOT NULL," +
                    "  NAME varchar(255) NOT NULL," +
                    "  BROKER_NAME varchar(255) NOT NULL," +
                    "  CREATE_TIME timestamp DEFAULT current timestamp" +
                    ")";

            statement.execute(createTable);

            String select = "select * from queue";

            boolean execute = statement.execute(select);

            ResultSet resultSet = statement.getResultSet();
            System.out.println(resultSet);


            CountDownLatch latch = new CountDownLatch(1);
            latch.await();
        }catch(Exception e) {
            e.printStackTrace();
        }finally {

        }


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

}