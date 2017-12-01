package com.liutaoyxz.yxzmq.cluster.zookeeper;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Doug Tao
 * @Date: 10:54 2017/11/28
 * @Description:
 */
public class ZkBrokerRootTest {
    @Test
    public void checkRoot() throws Exception {
//        ZkBrokerRoot root = new ZkBrokerRoot();
//        root.checkRoot();
    }

    @Test
    public void getIp() throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().getHostAddress());
    }

    @Test
    public void register() throws InterruptedException {
//        ZkBrokerRoot root = new ZkBrokerRoot();
//        root.register(11171);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }

    @Test
    public void brokers() throws Exception{
//        ZkBrokerRoot root = new ZkBrokerRoot();
//        List<String> brokers = root.brokers();
//        System.out.println(brokers);
//        CountDownLatch latch = new CountDownLatch(1);
//        latch.await();
    }

    @Test
    public void stcreatart() throws Exception{
        ZkBrokerRoot root = new ZkBrokerRoot(11173);
        root.start();
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }


}