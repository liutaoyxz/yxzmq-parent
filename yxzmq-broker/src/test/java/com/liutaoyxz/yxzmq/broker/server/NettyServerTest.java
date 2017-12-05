package com.liutaoyxz.yxzmq.broker.server;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 16:05 2017/12/5
 * @Description:
 */
public class NettyServerTest {
    @Test
    public void start() throws Exception {
        NettyServer server = new NettyServer();
        server.start();
        server.connect("127.0.0.1",11171);
        server.connect("127.0.0.1",11171);
        server.connect("127.0.0.1",11171);
        server.connect("127.0.0.1",11171);
        server.connect("127.0.0.1",11171);
        Thread.sleep(100000);

    }

    @Test
    public void connect() throws Exception {
//        CountDownLatch latch = new CountDownLatch(1);
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        lock.lock();
        try {
            condition.await();

        }finally {
            System.out.println("xxxxx");
        }


    }

}