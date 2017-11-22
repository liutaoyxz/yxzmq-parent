package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.client.session.YxzDefaultSession;
import com.liutaoyxz.yxzmq.client.session.YxzTextMessage;
import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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

    private List<YxzClientChannel> channels = new CopyOnWriteArrayList<>();

    private BlockingQueue<YxzClientChannel> activeChannels;

    private YxzClientChannel assistChannel;

    /**
     * 主题监听器列表
     */
    private ConcurrentHashMap<String,CopyOnWriteArrayList<TopicSubscriber>> topicListener;

    private ReentrantLock topicListenerLock = new ReentrantLock();

    /**
     * 队列监听列表
     */
    private ConcurrentHashMap<String,CopyOnWriteArrayList<QueueReceiver>> queueListener;

    private ReentrantLock queueListenerLock = new ReentrantLock();

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

    /**
     * broker 端返回的id,通过辅助通道申请的
     */
    private String groupId;

    /**
     * 辅助channel 计数器
     */
    private CountDownLatch assistRegisterCountDownLatch = new CountDownLatch(1);

    /**
     * 主channel 计数器
     */
    private CountDownLatch registerCountDownLatch ;

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
        this.registerCountDownLatch = new CountDownLatch(channelNum);
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
        this.topicListener = new ConcurrentHashMap<>();
        this.queueListener = new ConcurrentHashMap<>();
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
            // TODO: 2017/11/20 注册辅助channel和活跃channel

            this.assistRegister();
            this.assistRegisterCountDownLatch.await();
            log.debug("assistRegister success");
            this.mainRegister();
            this.registerCountDownLatch.await();
            log.debug("main register success");
            this.connected = true;
            final String clientID = YxzDefaultConnection.this.getClientID();
            log.debug("connection start,clientID is {}", clientID);
        } catch (IOException e) {
            log.debug("start connection error", e);
            JMSException jmsException = JMSErrorEnum.CONNECT_ERROR.exception(e);
            throw jmsException;
        }
        catch (InterruptedException e) {
            log.debug("start connection error", e);
            JMSException jmsException = JMSErrorEnum.CONNECT_ERROR.exception(e);
            throw jmsException;
        }
        finally {
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
            assistChannel = new YxzClientChannel(this,SocketChannel.open(),true);
            this.channels.add(assistChannel);
            SocketChannel asc = assistChannel.getChannel();
            YxzDefaultConnectionFactory.registerSocketChannel(asc);
            asc.connect(address);
            for (int i = 0; i < channelNum; i++) {
                YxzClientChannel sc = new YxzClientChannel(this,SocketChannel.open(),false);
                YxzDefaultConnectionFactory.registerSocketChannel(sc.getChannel());
                sc.getChannel().connect(address);
                this.channels.add(sc);
            }
            YxzDefaultConnectionFactory.startSelect();
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

    /**
     * 申请一个channel,会阻塞
     * @return
     */
    YxzClientChannel applyChannel(){
        try {
            return activeChannels.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回一个channel
     * @param socketChannel
     */
    void returnChannel(YxzClientChannel socketChannel){
        this.activeChannels.add(socketChannel);
    }

    void assistRegisterDown(){
        this.assistRegisterCountDownLatch.countDown();
    }

    void registerDown(YxzClientChannel channel){
        this.registerCountDownLatch.countDown();
        try {
            this.activeChannels.put(channel);
        } catch (InterruptedException e) {
            log.debug("add activeChannel error",e);
        }
    }

    void setGroupId(String groupId){
        this.groupId = groupId;
    }

    String groupId(){
        return this.groupId;
    }

    void assistRegister() throws IOException {
        YxzClientChannel assistChannel = this.assistChannel;
        SocketChannel sc = assistChannel.getChannel();
        ProtocolBean bean = new ProtocolBean();
        bean.setCommand(CommonConstant.Command.ASSIST_REGISTER);
        Metadata metadata = new Metadata();
        List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, null, bean);
        for (byte[] b : bytes){
            ByteBuffer buffer = ByteBuffer.wrap(b);
            sc.write(buffer);
            while (buffer.hasRemaining()){
                sc.write(buffer);
            }
        }
    }

    /**
     * 主channel 注册
     * @throws IOException
     */
    void mainRegister() throws IOException {
        for (YxzClientChannel yc : channels){
            if (yc.isRegistered()){
                continue;
            }
            SocketChannel sc = yc.getChannel();
            ProtocolBean bean = new ProtocolBean();
            bean.setGroupId(groupId);
            bean.setCommand(CommonConstant.Command.MAIN_REGISTER);
            Metadata metadata = new Metadata();
            List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, null, bean);
            for (byte[] b : bytes){
                ByteBuffer buffer = ByteBuffer.wrap(b);
                sc.write(buffer);
                while (buffer.hasRemaining()){
                    sc.write(buffer);
                }
            }
        }
    }

    public void addTopicSubscriber(TopicSubscriber subscriber){
        topicListenerLock.lock();
        try {
            String topicName = subscriber.getTopic().getTopicName();
            CopyOnWriteArrayList<TopicSubscriber> list = this.topicListener.get(topicName);
            if (list == null){
                list = new CopyOnWriteArrayList<>();
                list.add(subscriber);
                this.topicListener.put(topicName,list);
                return;
            }
            list.add(subscriber);
        }catch (JMSException e){
            log.debug("addTopicSubscriber error",e);
        }finally {
            topicListenerLock.unlock();
        }
    }

    /**
     * 发布消息
     * @param type
     * @param title
     * @param text
     */
    void sendMessageToConsumer(int type,String title,String text){
        try {
            if (type == CommonConstant.MessageType.TOPIC){
                //主题
                CopyOnWriteArrayList<TopicSubscriber> subscribers = this.topicListener.get(title);
                for (TopicSubscriber s : subscribers){
                    MessageListener listener = s.getMessageListener();
                    listener.onMessage(new YxzTextMessage(text));
                }
            }else {
                //队列
            }
        }catch (JMSException e){
            log.debug("sendMessageToConsumer error",e);
        }

    }

}
