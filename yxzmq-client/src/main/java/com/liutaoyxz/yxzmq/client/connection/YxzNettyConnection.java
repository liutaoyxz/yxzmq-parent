package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkClientRoot;
import com.liutaoyxz.yxzmq.common.enums.ConnectStatus;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.liutaoyxz.yxzmq.common.enums.ConnectStatus.NOT_CONNECT;
import static com.liutaoyxz.yxzmq.common.enums.ConnectStatus.NOT_REGISTER;

/**
 * @author Doug Tao
 * @Date 下午7:37 2017/12/10
 * @Description:
 */
public class YxzNettyConnection extends AbstractConnection {

    private static final Logger log = LoggerFactory.getLogger(YxzNettyConnection.class);


    /**
     * 执行session,具体执行方式在session中
     */
    private ExecutorService sessionExecutor;

    /**
     * connection 自身的任务执行器
     */
    private ExecutorService connectionExecutor;

    /**
     * 是否停止
     */
    private volatile boolean stop = false;

    /** 是否已经有准备好的broker了 **/
    private volatile boolean haveReadyBroker = false;

    private ReentrantLock lock = new ReentrantLock();

    private Condition brokerCondition = lock.newCondition();

    private ZkClientRoot root;

    private ZkClientListener listener;

    /** zookeeper 连接字符串 **/
    private String zookeeperStr;

    /** 这个连接在zookeeper端的名字 **/
    private String myName;

    private YxzNettyConnectionFactory factory;
    /** 所有的broker **/
    private ConcurrentHashMap<String,BrokerConnection> allBrokers = new ConcurrentHashMap<>();
    /** 可用的broker,我已经注册成功的 **/
    private ConcurrentHashMap<String,BrokerConnection> readyBrokers = new ConcurrentHashMap<>();

    YxzNettyConnection(String zookeeperStr,YxzNettyConnectionFactory factory){
        this.factory = factory;
        this.zookeeperStr = zookeeperStr;
        this.listener = new ZkClientListener();
        this.sessionExecutor = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("session-executor");
                return thread;
            }
        });

    }

    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        return null;
    }

    /**
     * clientID , 全局唯一
     * @param clientID 采用zookeeper 顺序分配,不会重复
     * @throws JMSException 不支持自己配置
     */
    @Override
    public void setClientID(String clientID) throws JMSException {
        throw JMSErrorEnum.OP_NOT_SUPPORT.exception();
    }

    @Override
    public void start() throws JMSException {
        lock.lock();
        try {
            if (root == null){
                root = ZkClientRoot.createRoot(listener,zookeeperStr);
                List<Broker> brokers = root.start();
                this.myName = root.getMyName();
                int count = this.connectBrokers(brokers);
                if (count == 0){
                    throw JMSErrorEnum.NO_BROKER.exception();
                }
                waitBroker();
            }
        } catch (Exception e) {
            log.error("start error ",e);
            throw JMSErrorEnum.CONNECTION_START_ERROR.exception(e);
        } finally {
            lock.unlock();
        }

    }

    /**
     * 等待注册成功的通知,只要有一个broker可用就返回
     */
    private void waitBroker() throws InterruptedException {
        if (!haveReadyBroker){
            lock.lock();
            try {
                brokerCondition.await();
            }finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void stop() throws JMSException {

    }

    @Override
    public void close() throws JMSException {

    }

    String myName(){
        return this.myName;
    }

    /**
     * 连接到broker
     * @param ip
     * @param port
     * @return
     */
    public boolean connectBroker(BrokerConnection conn){
        NioSocketChannel channel = null;
        String ip = conn.getIp();
        int port = conn.getPort();
        try {
            channel = factory.connectBroker(ip,port,this);
        } catch (InterruptedException e) {
            log.warn("connect to broker [{}] error",conn.getName());
            return false;
        }
        conn.setChannel(channel);
        return true;
    }

    private int connectBrokers(List<Broker> brokers) throws InterruptedException {
        int result = 0;
        for (Broker b : brokers){
            BrokerConnection conn = new BrokerConnection(b.getName(),this);
            //连接并且发送注册消息
            if (conn.connectAndRegister()){
                result ++;
            }
        }
        return result;
    }
}
