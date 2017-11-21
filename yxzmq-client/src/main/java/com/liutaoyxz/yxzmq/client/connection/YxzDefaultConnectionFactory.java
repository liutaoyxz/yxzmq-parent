package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date 下午9:32 2017/11/18
 * @Description: 采用单例模式
 * 一个连接对应一个或者多个channel
 * 连接在start()之后与broker 开始连接,保存到ChannelContainer中
 *
 */
@JMSConnectionFactory("yxz")
public class YxzDefaultConnectionFactory implements ConnectionFactory{


    /**
     * 默认ip
     */
    private static final String DEFAULT_ADDRESS = "127.0.0.1:11171";

    private static final YxzDefaultConnectionFactory FACTORY = new YxzDefaultConnectionFactory();

    public static final Logger log = LoggerFactory.getLogger(YxzDefaultConnectionFactory.class);

    private static ExecutorService selectExecutor = new ThreadPoolExecutor(1, 1, 5L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("factory-task");
            return thread;
        }
    });

    private static Selector selector;

    private static FactoryTask task;

    /**
     * 定时任务,心跳测试,暂时没用,只是保证程序不退出
     */
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("scheduled-task");
            return thread;
        }
    });

    private final CopyOnWriteArrayList<YxzDefaultConnection> connections = new CopyOnWriteArrayList<>();

    private volatile boolean started = false;

    private ReentrantLock startLock = new ReentrantLock();

    private YxzDefaultConnectionFactory(){

    }

    public static YxzDefaultConnectionFactory getFactory(){
        return FACTORY;
    }

    @Override
    public Connection createConnection() throws JMSException {
        return createConnection(DEFAULT_ADDRESS,null);
    }

    private void checkStarted(){
        if (!started){
            startLock.lock();
            try {
                selector = Selector.open();
                task = new FactoryTask(selector);
                executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        log.debug("factory started");
                    }
                },0L, TimeUnit.SECONDS);
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                startLock.unlock();
            }
        }
    }

    static void startSelect(){
        selectExecutor.execute(task);
        task.start();
    }

    static void stopSelect(){
        task.stop();
    }

    /**
     * 创建一个connection
     * @param address 地址  ip
     * @param str 暂时不用
     * @return
     * @throws JMSException
     */
    @Override
    public Connection createConnection(String address, String str) throws JMSException {
        checkStarted();
        YxzDefaultConnection connection = new YxzDefaultConnection(3, createAddress(address));
        try {
            connection.setClientID(ConnectionContainer.createClientID());
            connection.init();
            ConnectionContainer.addConnection(connection);
        } catch (IOException e) {
            log.debug("socketChannel open error",e);
            JMSException exception = JMSErrorEnum.CHANNEL_OPEN_ERROR.exception();
            exception.setLinkedException(e);
            throw exception;
        }
        return connection;
    }

    private InetSocketAddress createAddress(String address){
        String[] ss = address.split(":");
        String ip = ss[0];
        int port = Integer.valueOf(ss[1]);
        return new InetSocketAddress(ip,port);
    }

    /**
     * 把socketChannel交给selector管理
     * @param channel
     * @throws IOException
     */
    static void registerSocketChannel(SocketChannel channel) throws IOException {
        channel.configureBlocking(false);
//        selector.wakeup();
        channel.register(selector, SelectionKey.OP_CONNECT, ByteBuffer.allocate(128));
        log.debug("after register");
    }

    @Override
    public JMSContext createContext() {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1) {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1, int i) {
        return null;
    }

    @Override
    public JMSContext createContext(int i) {
        return null;
    }
}
