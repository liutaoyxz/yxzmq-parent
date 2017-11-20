package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.client.session.YxzDefaultSession;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date 下午10:11 2017/11/18
 * @Description:
 */
public class YxzDefaultConnection extends AbstractConnection {

    private static final AtomicInteger AUTO_INCREASE_CONNECTION_ID = new AtomicInteger(1);

    private final Logger log = LoggerFactory.getLogger(YxzDefaultConnection.class);

    private CopyOnWriteArrayList<YxzDefaultSession> sessions = new CopyOnWriteArrayList<>();

    private List<SocketChannel> channels = new CopyOnWriteArrayList<>();

    private BlockingQueue<SocketChannel> activeChannels;

    private SocketChannel assistChannel;

    /**
     * 执行session,具体执行方式在session中
     */
    private ExecutorService sessionExecutor;

    /**
     * connection 自身的任务执行器
     */
    private ExecutorService connectionExecutor;

    /**
     * 是否连接到broker
     */
    private volatile boolean connected = false;

    /**
     * 是否停止
     */
    private volatile boolean stop = false;

    /**
     * 是否关闭
     */
    private volatile boolean close = false;

    private boolean inited = false;

    private ReentrantLock lock = new ReentrantLock();

//    private Condition wait = lock.newCondition();



    /**
     * 地址
     */
    private InetSocketAddress address;

    private int channelNum = 1;

    public YxzDefaultConnection(int channelNum, InetSocketAddress address) {
        if (channelNum <= 0) {
            throw new IllegalArgumentException("channelNum can not be " + channelNum);
        }
        if (address == null) {
            throw new NullPointerException("address can not be null");
        }
        this.channelNum = channelNum;
        this.address = address;

        this.sessionExecutor = new ThreadPoolExecutor(10, Integer.MAX_VALUE, 10L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                try {
                    String clientID = getClientID();
                    thread.setName("session-executor-" + clientID);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                return thread;
            }
        });

        this.connectionExecutor = new ThreadPoolExecutor(channelNum, channelNum, 5L,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                try {
                    String clientID = getClientID();
                    thread.setName("connection-executor-" + clientID);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                return thread;
            }
        });
        this.activeChannels = new ArrayBlockingQueue(channelNum);

    }

    /**
     * 创建一个session,实质上就是一个runnable 任务
     *
     * @param transacted      是否是事物
     * @param acknowledgeMode 打招呼模式
     * @return
     * @throws JMSException
     */
    @Override
    public Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
        if (!connected){
            throw JMSErrorEnum.CONNECTION_NOT_START.exception();
        }
        YxzDefaultSession session = new YxzDefaultSession(this);
        this.sessions.add(session);
        this.sessionExecutor.execute(session);
        return session;
    }

    /**
     * 添加一个任务
     *
     * @param task
     * @throws JMSException
     */
    public void addTask(YxzConnectionTask task) throws JMSException {
        if (connected) {
            this.connectionExecutor.execute(task);
        }
    }

    /**
     * 开始连接
     * 阻塞本线程
     *
     * @throws JMSException
     */
    @Override
    public void start() throws JMSException {
        lock.lock();
        try {
            if (!inited) {
                throw JMSErrorEnum.CONNECTION_NOT_INIT.exception();
            }
            ConnectionContainer.scMap(clientID, channels);
            ConnectionContainer.connect(getClientID(), address);
            // TODO: 2017/11/20 注册辅助channel和活跃channel
            activeChannels.addAll(channels);
            this.connected = true;
            final String clientID = YxzDefaultConnection.this.getClientID();
            log.debug("connection start,clientID is {}", clientID);
        } catch (IOException e) {
            log.debug("start connection error", e);
            JMSException jmsException = JMSErrorEnum.CONNECT_ERROR.exception();
            jmsException.setLinkedException(e);
            throw jmsException;
        } finally {
            lock.unlock();
        }

    }

    /**
     * 停止发送数据,但是连接不关闭,不能够创建新的session
     *
     * @throws JMSException
     */
    @Override
    public void stop() throws JMSException {

    }

    /**
     * 关闭连接 ,不能再创建session,已经存在的session需要处理完成
     *
     * @throws JMSException
     */
    @Override
    public void close() throws JMSException {

    }

    void init() throws IOException {
        lock.lock();
        try {
            if (inited) {
                log.debug("already inited");
                return;
            }
            assistChannel = SocketChannel.open();
            for (int i = 0; i < channelNum; i++) {
                SocketChannel sc = SocketChannel.open();
                this.channels.add(sc);
            }
        } finally {
            this.inited = true;
            lock.unlock();
        }
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        if (inited) {
            return;
        }
        this.clientID = clientID;
    }

    List<SocketChannel> getChannels(){
        return this.channels;
    }

    SocketChannel applyChannel(){
        try {
            return activeChannels.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    void returnChannel(SocketChannel socketChannel){
        this.activeChannels.add(socketChannel);
    }

}
