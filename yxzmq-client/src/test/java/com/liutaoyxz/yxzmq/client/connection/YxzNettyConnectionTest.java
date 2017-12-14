package com.liutaoyxz.yxzmq.client.connection;

import org.junit.Test;

import javax.jms.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 17:19 2017/12/14
 * @Description:
 */
public class YxzNettyConnectionTest {
    @Test
    public void start() throws Exception {
        YxzNettyConnectionFactory factory = new YxzNettyConnectionFactory("127.0.0.1:2181");
        Connection conn = factory.createConnection();
        conn.start();
    }

    @Test
    public void atomicTest(){
        AtomicBoolean b = new AtomicBoolean(false);
        while (!b.compareAndSet(true,true)){

        }

    }

}