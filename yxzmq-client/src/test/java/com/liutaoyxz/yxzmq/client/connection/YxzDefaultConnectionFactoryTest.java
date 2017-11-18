package com.liutaoyxz.yxzmq.client.connection;

import org.junit.Test;

import javax.jms.Connection;

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
        connection.start();
    }



}