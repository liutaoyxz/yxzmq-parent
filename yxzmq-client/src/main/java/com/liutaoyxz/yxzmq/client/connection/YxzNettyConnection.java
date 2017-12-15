package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.client.YxzClientContext;
import com.liutaoyxz.yxzmq.client.session.YxzNettySession;
import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkClientRoot;
import com.liutaoyxz.yxzmq.common.enums.ConnectStatus;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

import java.io.IOException;
import java.util.ArrayList;
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

    /** 等待broker注册成功 **/
    private Condition brokerCondition = lock.newCondition();

    /** 关闭等待 **/
    private Condition closeCondition = lock.newCondition();

    private ZkClientRoot root;

    private ZkClientListener listener;

    /** zookeeper 连接字符串 **/
    private String zookeeperStr;

    /** 这个连接在zookeeper端的名字 **/
    private String myName;
    /** 是否启动 **/
    private boolean started = false;

    private YxzNettyConnectionFactory factory;

    private YxzClientContext ctx;

    /** 所有的broker,id和conn 对应 **/
    private ConcurrentHashMap<String,BrokerConnection> allBrokers = new ConcurrentHashMap<>();
    /** 可用的broker,我已经注册成功的 ,zkName 和 conn 对应 **/
    private ConcurrentHashMap<String,BrokerConnection> readyBrokers = new ConcurrentHashMap<>();

    YxzNettyConnection(String zookeeperStr,YxzNettyConnectionFactory factory){
        this.factory = factory;
        this.ctx = new YxzClientContext(factory);
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

    /**
     * create session
     * @param transacted 是否是事务session
     * @param acknowledgeMode 问答模式
     * @return
     * @throws JMSException
     */
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        ctx.setConnection(this);
        YxzNettySession session = new YxzNettySession(ctx,transacted,acknowledgeMode);
        return session;
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
        if (started){
            throw JMSErrorEnum.CONNECTION_ALREADY_STARTED.exception();
        }
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
                log.info("client [{}] started ... brokers is {}",myName,readyBrokerNames());
                started = true;
            }
        }catch (JMSException e){
          throw e;
        } catch (Exception e) {
            log.error("start error ",e);
            throw JMSErrorEnum.CONNECTION_START_ERROR.exception(e);
        } finally {
            lock.unlock();
        }

    }


    private List<String> readyBrokerNames(){
        List<String> result = new ArrayList<>();
        ConcurrentHashMap.KeySetView<String, BrokerConnection> view = readyBrokers.keySet();
        for (String zkName : view){
            result.add(zkName);
        }
        return result;
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
        allBrokers.put(conn.id(),conn);
        return true;
    }

    public void read(byte[] bytes,String id) throws InterruptedException {
        BrokerConnection broker = allBrokers.get(id);
        List<ProtocolBean> beans = broker.read(bytes);
        log.info("read beans {}",beans);
        for (ProtocolBean bean : beans){
            handleBean(bean,id);
        }
    }


    public void handleBean(ProtocolBean bean,String id) throws InterruptedException {
        int command = bean.getCommand();
        String zkName = bean.getZkName();
        BrokerConnection conn = allBrokers.get(id);
        switch (command){

            case CommonConstant.Command.REGISTER_SUCCESS:
                //注册成功
                conn.registerSuccess();
                readyBrokers.put(conn.getName(),conn);
                log.info("register on broker [{}] success",conn.getName());
                signalBroker();
                break;
            case CommonConstant.Command.SEND:
                //发送消息


                break;
            default:
                break;
        }
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

    private void signalBroker() throws InterruptedException {
        if (!haveReadyBroker){
            lock.lock();
            try {
                brokerCondition.signalAll();
            }finally {
                lock.unlock();
            }
        }
    }

}
