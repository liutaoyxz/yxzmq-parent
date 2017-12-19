package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.client.YxzClientContext;
import com.liutaoyxz.yxzmq.client.session.YxzNettySession;
import com.liutaoyxz.yxzmq.cluster.broker.Broker;
import com.liutaoyxz.yxzmq.cluster.zookeeper.ZkClientRoot;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import com.liutaoyxz.yxzmq.common.util.ProtostuffUtil;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
     * 是否停止
     */
    private volatile boolean stop = false;

    /**
     * 是否已经有准备好的broker了
     **/
    private volatile boolean haveReadyBroker = false;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * 等待broker注册成功
     **/
    private Condition brokerCondition = lock.newCondition();

    /**
     * 关闭等待
     **/
    private Condition closeCondition = lock.newCondition();

    private ZkClientRoot root;

    private ZkClientListener listener;

    /**
     * session manager
     **/
    private SessionManager sessionManager;

    /**
     * zookeeper 连接字符串
     **/
    private String zookeeperStr;

    /**
     * 这个连接在zookeeper端的名字
     **/
    private String myName;
    /**
     * 是否启动
     **/
    private boolean started = false;

    private YxzNettyConnectionFactory factory;

    private YxzClientContext ctx;

    /**
     * 所有的broker,id和conn 对应
     **/
    private ConcurrentHashMap<String, BrokerConnection> allBrokers = new ConcurrentHashMap<>();
    /**
     * 可用的broker,我已经注册成功的 ,zkName 和 conn 对应
     **/
    private ConcurrentHashMap<String, BrokerConnection> readyBrokers = new ConcurrentHashMap<>();

    private AtomicInteger readyBrokerNum = new AtomicInteger(0);
    /**
     * 发送数据用的brokers
     **/
    private BlockingDeque<BrokerConnection> applyBrokers = new LinkedBlockingDeque<>();

    YxzNettyConnection(String zookeeperStr, YxzNettyConnectionFactory factory) {
        this.factory = factory;
        this.ctx = new YxzClientContext(factory);
        this.zookeeperStr = zookeeperStr;
        this.listener = new ZkClientListener();
        this.sessionManager = new SessionManager();
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
     *
     * @param transacted      是否是事务session
     * @param acknowledgeMode 问答模式
     * @return
     * @throws JMSException
     */
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        YxzNettySession session = new YxzNettySession(ctx.createCtx(this), transacted, acknowledgeMode);
        this.sessionManager.addSession(session);
        this.sessionExecutor.execute(session);
        return session;
    }

    /**
     * clientID , 全局唯一
     *
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
        if (started) {
            throw JMSErrorEnum.CONNECTION_ALREADY_STARTED.exception();
        }
        if (listener.getConnection() == null) {
            listener.setConnection(this);
        }
        try {
            if (root == null) {
                root = ZkClientRoot.createRoot(listener, zookeeperStr);
                List<Broker> brokers = root.start();
                this.myName = root.getMyName();
                int count = this.connectBrokers(brokers);
                if (count == 0) {
                    throw JMSErrorEnum.NO_BROKER.exception();
                }
                waitBroker();
                log.info("client [{}] started ... brokers is {}", myName, readyBrokerNames());
                started = true;
            }
        } catch (JMSException e) {
            throw e;
        } catch (Exception e) {
            log.error("start error ", e);
            throw JMSErrorEnum.CONNECTION_START_ERROR.exception(e);
        } finally {
            lock.unlock();
        }

    }


    private List<String> readyBrokerNames() {
        List<String> result = new ArrayList<>();
        ConcurrentHashMap.KeySetView<String, BrokerConnection> view = readyBrokers.keySet();
        for (String zkName : view) {
            result.add(zkName);
        }
        return result;
    }

    /**
     * 等待注册成功的通知,只要有一个broker可用就返回
     */
    private void waitBroker() throws InterruptedException {
        if (!haveReadyBroker) {
            lock.lock();
            try {
                brokerCondition.await();
            } finally {
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

    public String myName() {
        return this.myName;
    }

    /**
     * 连接到broker
     *
     * @param conn
     * @return
     */
    public boolean connectBroker(BrokerConnection conn) {
        NioSocketChannel channel = null;
        String ip = conn.getIp();
        int port = conn.getPort();
        try {
            channel = factory.connectBroker(ip, port, this);
        } catch (InterruptedException e) {
            log.warn("connect to broker [{}] error", conn.getName());
            return false;
        }
        conn.setChannel(channel);
        allBrokers.put(conn.id(), conn);
        return true;
    }

    public void read(byte[] bytes, String id) throws InterruptedException {
        BrokerConnection broker = allBrokers.get(id);
        List<ProtocolBean> beans = broker.read(bytes);
        log.debug("read beans {}", beans);
        for (ProtocolBean bean : beans) {
            handleBean(bean, id);
        }
    }

    public boolean write(List<byte[]> bytes) throws JMSException {
        BrokerConnection conn = null;
        try {
            conn = this.applyConnection();
            while (!conn.send(bytes, true)) {
                checkAndReturn(conn);
                conn = this.applyConnection();
            }
            return true;
        } catch (JMSException e) {
            log.error("write data error", e);
            throw e;
        } catch (Exception e) {
            log.error("write data error", e);
            return write(bytes);
        } finally {
            checkAndReturn(conn);
        }
    }

    /**
     * 检查conn 是否可用
     *
     * @param conn
     */
    private void checkAndReturn(BrokerConnection conn) {
        if (conn != null && conn.isActive()) {
            if (conn.isActive()) {
                applyBrokers.add(conn);
                return;
            }
            readyBrokers.remove(conn.getName());
            allBrokers.remove(conn.id());
            readyBrokerNum.decrementAndGet();
        }

    }

    /**
     * 申请写的conn
     *
     * @return
     * @throws JMSException
     */
    private BrokerConnection applyConnection() throws JMSException, InterruptedException {
        if (readyBrokerNum.get() <= 0) {
            throw JMSErrorEnum.NO_BROKER.exception();
        }
        BrokerConnection conn = applyBrokers.take();
        if (readyBrokers.get(conn.getName()) == conn) {
            return conn;
        }
        return applyConnection();
    }


    public void delBroker(String id) throws Exception {
        BrokerConnection conn = allBrokers.remove(id);
        if (conn != null) {
            log.info("remove brokerConnection {}", conn.getName());
            conn.close();
            BrokerConnection rconn = readyBrokers.remove(conn.getName());
            if (conn == rconn) {
                readyBrokerNum.decrementAndGet();
            }
        }
    }

    public List<String> delBrokers(List<Broker> brokers){
        log.info("del brokers {}",brokers);
        List<String> result = new ArrayList<>();
        for (Broker b : brokers){
            BrokerConnection conn = readyBrokers.remove(b.getName());
            if (conn != null) {
                result.add(conn.getName());
                log.info("remove brokerConnection {}", conn);
                BrokerConnection rconn = allBrokers.remove(conn.id());
                if (conn == rconn) {
                    readyBrokerNum.decrementAndGet();
                }
                try {
                    conn.close();
                } catch (Exception e) {
                    log.warn("close conn {} error",conn);
                }
            }
        }
        return result;
    }

    /**
     * 处理收到的消息
     *
     * @param bean
     * @param id
     * @throws InterruptedException
     */
    public void handleBean(ProtocolBean bean, String id) throws InterruptedException {
        int command = bean.getCommand();
        String zkName = bean.getZkName();
        BrokerConnection conn = allBrokers.get(id);
        switch (command) {

            case CommonConstant.Command.REGISTER_SUCCESS:
                //注册成功
                conn.registerSuccess();
                BrokerConnection put = readyBrokers.put(conn.getName(), conn);
                readyBrokerNum.incrementAndGet();
                applyBrokers.add(conn);
                log.info("register on broker [{}] success", conn.getName());
                signalBroker();
                root.addConnectBroker(conn.getName());
                break;
            case CommonConstant.Command.SEND:
                //发送消息
                handleMessage(bean);
                break;
            default:
                break;
        }
    }

    private void handleMessage(ProtocolBean bean) {
        String text = getMessage(bean);
        MessageDesc desc = getDesc(bean);
        int type = desc.getType();
        String title = desc.getTitle();
        boolean h = false;
        if (type == CommonConstant.MessageType.QUEUE) {
            //queue
            while (!h){
                BlockingDeque<YxzNettySession> sessions = sessionManager.queueSession.get(title);
                synchronized (sessions) {
                    if (sessions.isEmpty()){
                        // TODO: 2017/12/18 这个连接已经不再监听这个queue了,通知broker发送给别的session
                        log.warn("receive queue message {},bug no session",text);
                        return;
                    }
                    YxzNettySession session = sessions.peek();
                    if (session != null) {
                        h = session.handleQueueMessage(title, text);
                        if (h) {
                            YxzNettySession poll = sessions.poll();
                            if (poll != null) {
                                sessions.add(poll);
                            }
                        }
                    }
                }
            }
        }
        if (type == CommonConstant.MessageType.TOPIC) {
            //topic


        }
    }

    /**
     * 反序列化 MessageDesc
     *
     * @param bean ProtocolBean
     * @return null 或者 desc 实例
     */
    private static MessageDesc getDesc(ProtocolBean bean) {
        if (StringUtils.isBlank(bean.getDescClass()) || bean.getDescBytes() == null) {
            return null;
        }
        return (MessageDesc) ProtostuffUtil.get(bean.getDescBytes(), bean.getDescClass());
    }

    /**
     * 反序列化 Message
     *
     * @param bean ProtocolBean
     * @return null 或者 消息字符串
     */
    private static String getMessage(ProtocolBean bean) {
        if (bean.getDataBytes() == null) {
            return null;
        }

        return new String(bean.getDataBytes(), Charset.forName(ReadContainer.DEFAULT_CHARSET));
    }

    private int connectBrokers(List<Broker> brokers) throws InterruptedException {
        int result = 0;
        for (Broker b : brokers) {
            BrokerConnection conn = new BrokerConnection(b.getName(), this);
            //连接并且发送注册消息
            if (conn.connectAndRegister()) {
                result++;
            }
        }
        return result;
    }

    private void signalBroker() throws InterruptedException {
        if (!haveReadyBroker) {
            lock.lock();
            try {
                brokerCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 增加一个broker
     *
     * @param brokers
     */
    public List<String> addBrokers(List<Broker> brokers) {
        List<String> result = new ArrayList<>();
        for (Broker b : brokers) {
            BrokerConnection conn = new BrokerConnection(b.getName(), this);
            //连接并且发送注册消息
            try {
                boolean cb = conn.connectAndRegister();
                if (cb){
                    result.add(b.getName());
                }
            } catch (InterruptedException e) {
                log.error("connect to broker [{}] error",conn.getName());
            }
        }
        return result;
    }

    /**
     * 退订
     *
     * @param topicName
     * @param session
     */
    public void cancelSubscribe(String topicName, YxzNettySession session) {
        this.sessionManager.cancelSubscribe(session, topicName);
    }

    /**
     * 取消监听
     *
     * @param queueName
     * @param session
     */
    public void cancelListen(String queueName, YxzNettySession session) {
        this.sessionManager.cancelListen(session, queueName);
    }

    /**
     * 订阅主题
     *
     * @param topicName
     * @param session
     */
    public void subscribe(String topicName, YxzNettySession session) {
        this.sessionManager.subscriber(session, topicName);
    }

    /**
     * 监听queue
     *
     * @param queueName
     * @param session
     */
    public void listen(String queueName, YxzNettySession session) {
        this.sessionManager.listen(session, queueName);
    }

    public void cleanAndRemoveSession(YxzNettySession session) {
        this.sessionManager.delSession(session);
    }

    /**
     * 管理session 和connection 的关系
     * 每次订阅的主题或者监听的队列client端都需要知道具体是哪一个session
     * 当接收到相关的消息时,通过 SessionManager 查找通知哪一个session
     */
    private class SessionManager {

        /**
         * queue-session 对应的关系
         */
        private ConcurrentHashMap<String, BlockingDeque<YxzNettySession>> queueSession = new ConcurrentHashMap<>();

        /**
         * 主题,对应的session
         */
        private ConcurrentHashMap<String, Set<YxzNettySession>> topicSession = new ConcurrentHashMap<>();

        /**
         * session-queue 关系
         */
        private ConcurrentHashMap<YxzNettySession, Set<String>> sessionQueue = new ConcurrentHashMap<>();

        /**
         * session-topic 关系
         */
        private ConcurrentHashMap<YxzNettySession, Set<String>> sessionTopic = new ConcurrentHashMap<>();

        /**
         * 所有的session
         */
        private ConcurrentSet<YxzNettySession> sessions = new ConcurrentSet<>();

        public void addSession(YxzNettySession session) {
            sessions.add(session);
        }

        /**
         * 锁
         **/
        private ReentrantLock lock = new ReentrantLock();

        /**
         * session 订阅主题
         *
         * @param session
         * @param topicName
         * @throws JMSException
         */
        public synchronized void subscriber(YxzNettySession session, String topicName) {
            if (session == null || StringUtils.isBlank(topicName)) {
                throw new NullPointerException();
            }
            Set<String> topics = sessionTopic.get(session);
            if (topics == null) {
                topics = new ConcurrentSet<>();
                sessionTopic.put(session, topics);
            }
            topics.add(topicName);
            Set<YxzNettySession> ss = topicSession.get(topicName);
            if (ss == null) {
                ss = new ConcurrentSet<>();
                topicSession.put(topicName, ss);
            }
            if (ss.isEmpty()) {
                /**
                 * 通知 zookeeper,如果之前这个连接没有任何session订阅,就通知zookeeper
                 */
                YxzNettyConnection.this.root.subscribe(topicName);
            }
            ss.add(session);
        }

        /**
         * 监听queue
         *
         * @param session
         * @param queueName
         * @throws JMSException
         */
        public synchronized void listen(YxzNettySession session, String queueName) {
            if (session == null || StringUtils.isBlank(queueName)) {
                throw new NullPointerException();
            }
            Set<String> queues = sessionQueue.get(session);
            if (queues == null) {
                queues = new ConcurrentSet<>();
                sessionQueue.put(session, queues);
            }
            queues.add(queueName);
            BlockingDeque<YxzNettySession> ss = queueSession.get(queueName);
            if (ss == null) {
                ss = new LinkedBlockingDeque<>();
                queueSession.put(queueName, ss);
            }
            if (ss.isEmpty()) {
                /**
                 * 通知 zookeeper,如果之前这个连接没有任何session监听,就通知zookeeper
                 */
                YxzNettyConnection.this.root.listen(queueName);
            }
            ss.add(session);
        }

        /**
         * 取消监听
         *
         * @param session
         * @param queueName
         */
        public synchronized void cancelListen(YxzNettySession session, String queueName) {
            if (session == null || StringUtils.isBlank(queueName)) {
                throw new NullPointerException();
            }
            Set<String> queues = sessionQueue.get(session);
            if (queues != null) {
                queues.remove(queueName);
            }
            BlockingDeque<YxzNettySession> ss = queueSession.get(queueName);
            if (ss != null) {
                ss.remove(session);
            }
            if (ss == null || ss.isEmpty()) {
                //通知 zookeeper
                YxzNettyConnection.this.root.cancelListen(queueName);
            }
        }

        /**
         * 取消订阅
         *
         * @param session
         * @param topicName
         */
        public synchronized void cancelSubscribe(YxzNettySession session, String topicName) {
            if (session == null || StringUtils.isBlank(topicName)) {
                throw new NullPointerException();
            }
            Set<String> topics = sessionTopic.get(session);
            if (topics != null) {
                topics.remove(topicName);
            }
            Set<YxzNettySession> ss = topicSession.get(topicName);
            if (ss != null) {
                ss.remove(topicName);
            }
            if (ss == null || ss.isEmpty()) {
                //通知 zookeeper
                YxzNettyConnection.this.root.cancelSubscribe(topicName);
            }
        }

        /**
         * 删除session
         *
         * @param session
         */
        public synchronized void delSession(YxzNettySession session) {
            sessions.remove(session);
            Set<String> queueNames = sessionQueue.get(session);
            if (queueNames != null) {
                for (String name : queueNames) {
                    if (StringUtils.isNotBlank(name)) {
                        this.cancelListen(session, name);
                    }
                }
                sessionQueue.remove(session);
            }
            Set<String> topicNames = sessionTopic.get(session);
            if (topicNames != null) {
                for (String name : topicNames) {
                    if (StringUtils.isNotBlank(name)) {
                        this.cancelSubscribe(session, name);
                    }
                }
                sessionTopic.remove(session);
            }
        }

    }

}
