package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.common.Address;
import com.liutaoyxz.yxzmq.common.enums.ConnectStatus;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.liutaoyxz.yxzmq.common.enums.ConnectStatus.*;

/**
 * @author Doug Tao
 * @Date: 16:34 2017/12/13
 * @Description: broker的连接对象
 */
public class BrokerConnection implements AutoCloseable{

    private static final Logger log = LoggerFactory.getLogger(BrokerConnection.class);

    /**
     * broker 名, 端口:ip-排序
     **/
    private String name;

    /**
     * ip
     **/
    private String ip;

    /**
     * 端口
     **/
    private int port;

    /**
     * zookeeper分配的排序字符串
     **/
    private String order;

    /**
     * 连接状态
     */
    private ConnectStatus status;

    /**
     * 连接channel
     **/
    private NioSocketChannel channel;

    private String id;

    private YxzNettyConnection connection;

    private ReadContainer readContainer;

    public BrokerConnection(String name, YxzNettyConnection connection) {
        this.connection = connection;
        this.name = name;
        Address address = Address.createAddress(name);
        this.order = address.getOrder();
        this.ip = address.getIp();
        this.port = address.getPort();
        this.status = NOT_CONNECT;
        this.readContainer = new ReadContainer();
    }


    public void setChannel(NioSocketChannel channel) {
        this.channel = channel;
        this.id = channel.id().toString();
    }

    /**
     * 连接并且注册
     *
     * @return
     */
    public boolean connectAndRegister() throws InterruptedException {
        if (status == NOT_CONNECT && connection.connectBroker(this)) {
            this.status = NOT_REGISTER;
            return register();
        }
        if (status == NOT_REGISTER) {
            return register();
        }
        if (status == REGISTERED) {
            return true;
        }
        return false;
    }

    /**
     * 注册
     *
     * @return
     */
    private boolean register() throws InterruptedException {
        Metadata metadata = new Metadata();
        MessageDesc desc = new MessageDesc();
        ProtocolBean bean = new ProtocolBean();
        bean.setZkName(this.connection.myName());
        bean.setCommand(CommonConstant.Command.CLIENT_REGISTER);
        List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, desc, bean);
        return send(bytes,false);
    }

    public void registerSuccess(){
        this.status = REGISTERED;
    }

    public ConnectStatus getStatus() {
        return status;
    }

    /**
     * 发送数据
     *
     * @param bytes
     * @return
     */
    public boolean send(List<byte[]> bytes,boolean sync) throws InterruptedException {
        int length = 0;
        for (byte[] b : bytes) {
            length += b.length;
        }
        ByteBuf buf = channel.alloc().buffer(length);
        for (byte[] b : bytes) {
            buf.writeBytes(b);
        }
        ChannelFuture future = channel.writeAndFlush(buf);
        if (sync) {
            future.sync();
            return future.isSuccess();
        }
        return true;

    }

    public List<ProtocolBean> read(byte[] bytes){
        this.readContainer.read(bytes);
        return readContainer.flush();
    }

    public boolean isActive(){
        return status == REGISTERED && channel.isActive();
    }

    @Override
    public void close() throws Exception {
        this.status = CLOSED;
        channel.close();
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String id() {
        return id;
    }

    public String getOrder() {
        return order;
    }

    public NioSocketChannel channel() {
        return this.channel;
    }
}
