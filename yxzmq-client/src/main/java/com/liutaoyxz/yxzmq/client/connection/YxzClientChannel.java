package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 9:44 2017/11/21
 * @Description: 封装
 */
public class YxzClientChannel {

    public static final Logger log = LoggerFactory.getLogger(YxzClientChannel.class);

    /**
     * 读取容器
     */
    private ReadContainer readContainer = new ReadContainer();

    /**
     * channel
     */
    private SocketChannel channel;

    /**
     * 所属的connection
     */
    private YxzDefaultConnection parent;

    /**
     * 是否注册到broker
     */
    private boolean registered = false;

    /**
     * 是否正在读
     */
    private volatile boolean isReading = false;

    private ReentrantLock lock = new ReentrantLock();

    private boolean isAssistChannel;

    YxzClientChannel(YxzDefaultConnection parent,SocketChannel channel,boolean isAssistChannel){
        this.isAssistChannel = isAssistChannel;
        this.parent = parent;
        this.channel = channel;
    }

    public boolean startRead() {
        if (!isReading){
            lock.lock();
            try {
                if (!isReading){
                    if (channel == null){
                        throw new NullPointerException("startRead,but channel is null");
                    }
                    if (!channel.isOpen()){
                        log.info("startRead,but channel is not open,groupId is ",this.parent.groupId());
                    }
                    if (!channel.isConnected()){
                        log.info("startRead,but channel is not connected,groupId is {}",this.parent.groupId());
                    }
                    this.isReading = true;
                    log.debug("client start read,groupId is {}",this.parent.groupId());
                    return true;
                }
                return false;
            }finally {
                lock.unlock();
            }
        }
        return false;
    }


    public void read(ByteBuffer buffer) {
        log.debug("start read thread is {}",Thread.currentThread().getName());
        this.readContainer.read(buffer);
        stopRead();
        List<ProtocolBean> beans = this.readContainer.flush();
        for (ProtocolBean bean : beans){
            handlerBean(bean);
        }
    }

    public void stopRead() {
        log.debug("stop read,thread is {}",Thread.currentThread().getName());
        lock.lock();
        try {
            this.isReading = false;
        }finally {
            lock.unlock();
        }
    }



    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public YxzDefaultConnection getParent() {
        return parent;
    }

    public void setParent(YxzDefaultConnection parent) {
        this.parent = parent;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }


    /**
     * 处理协议
     * @param bean
     */
    private void handlerBean(ProtocolBean bean){
        int command = bean.getCommand();
        String groupId = bean.getGroupId();
        switch (command){
            case CommonConstant.Command.REGISTER_SUCCESS:
                if (isAssistChannel){
                    log.debug("assistChannel register success");
                    this.registered = true;
                    this.parent.setGroupId(groupId);
                    this.parent.assistRegisterDown();
                }else {
                    log.debug("mainChannel register success");
                    this.registered = true;
                    this.parent.registerDown(this);
                }
                break;
            default:
                log.debug("handlerBean error,command is {}",command);
                break;
        }


    }

    @Override
    public String toString() {
        return "YxzClientChannel{" +
                "channel=" + channel +
                ", parent=" + parent +
                ", registered=" + registered +
                '}';
    }
}
