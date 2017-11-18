package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.common.enums.JMSErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date 下午10:11 2017/11/18
 * @Description:
 */
public class YxzDefaultConnection extends AbstractConnection {

    private final Logger log = LoggerFactory.getLogger(YxzDefaultConnection.class);

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

    private List<SocketChannel> channels = new CopyOnWriteArrayList<>();

    /**
     * 地址
     */
    private InetSocketAddress address;

    private int channelNum = 1;

    public YxzDefaultConnection(int channelNum,InetSocketAddress address) {
        if (channelNum <= 0){
            throw new IllegalArgumentException("channelNum can not be "+channelNum);
        }
        if (address == null){
            throw new NullPointerException("address can not be null");
        }
        this.channelNum = channelNum;
        this.address = address;
    }

    /**
     * 创建一个session,实质上就是一个runnable 任务
     * @param b
     * @param i
     * @return
     * @throws JMSException
     */
    @Override
    public Session createSession(boolean b, int i) throws JMSException {
        return null;
    }

    /**
     * 开始连接
     * @throws JMSException
     */
    @Override
    public void start() throws JMSException {
        lock.lock();
        try {
            if (!inited){
                throw JMSErrorEnum.CONNECTION_NOT_INIT.exception();
            }
            ConnectionContainer.addConnections(clientID,channels);
            ConnectionContainer.connect(getClientID(),address);
            this.connected = true;
        }catch (IOException e){
            log.debug("start connection error",e);
            JMSException jmsException = JMSErrorEnum.CONNECT_ERROR.exception();
            jmsException.setLinkedException(e);
            throw jmsException;
        }finally {
            lock.unlock();
        }

    }

    /**
     * 停止发送数据,但是连接不关闭,不能够创建新的session
     * @throws JMSException
     */
    @Override
    public void stop() throws JMSException {

    }

    /**
     * 关闭连接 ,不能再创建session,已经存在的session需要处理完成
     * @throws JMSException
     */
    @Override
    public void close() throws JMSException {

    }

    void init() throws IOException {
        lock.lock();
        try {
            if (inited){
                log.debug("already inited");
                return;
            }
            for (int i = 0; i < channelNum; i++) {
                SocketChannel sc = SocketChannel.open();
                this.channels.add(sc);
            }
        }finally {
            this.inited = true;
            lock.unlock();
        }
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        if (inited){
            return;
        }
        this.clientID = clientID;
    }
}
