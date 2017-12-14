package com.liutaoyxz.yxzmq.client.connection;

import com.liutaoyxz.yxzmq.common.enums.ConnectStatus;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.liutaoyxz.yxzmq.common.enums.ConnectStatus.NOT_CONNECT;
import static com.liutaoyxz.yxzmq.common.enums.ConnectStatus.NOT_REGISTER;
import static com.liutaoyxz.yxzmq.common.enums.ConnectStatus.REGISTERED;

/**
 * @author Doug Tao
 * @Date: 16:34 2017/12/13
 * @Description: broker的连接对象
 */
public class BrokerConnection {

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

    public BrokerConnection(String name, YxzNettyConnection connection) {
        this.connection = connection;
        this.name = name;
        String[] split = StringUtils.split(name, "-");
        String hostIp = split[0];
        this.order = split[1];
        String[] address = StringUtils.split(hostIp, ":");
        this.ip = address[0];
        this.port = Integer.valueOf(address[1]);
        this.status = NOT_CONNECT;
    }

    /**
     * 连接到broker
     *
     * @return
     */
    private boolean connect() {
        if (status == REGISTERED || status == NOT_REGISTER) {
            return true;
        }

        return false;
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

    public ConnectStatus getStatus() {
        return status;
    }

    /**
     * 发送数据
     *
     * @param data
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
        }
        log.info("future {}", future);
        return true;
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
