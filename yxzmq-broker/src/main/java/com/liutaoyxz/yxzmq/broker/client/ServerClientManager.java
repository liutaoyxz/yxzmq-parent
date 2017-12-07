package com.liutaoyxz.yxzmq.broker.client;

import com.liutaoyxz.yxzmq.broker.server.NettyServer;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Doug Tao
 * @Date: 15:50 2017/12/5
 * @Description:
 */
public class ServerClientManager {

    /**
     * channel id 和 serverClient 对应的映射
     */
    private static final ConcurrentHashMap<String, ServerClient> ID_CLIENT = new ConcurrentHashMap<>();

    public static final Logger log = LoggerFactory.getLogger(ServerClientManager.class);

    private static String subjectId;

    private static String mirrorId;

    private static String myName;

    /**
     * 增加一个连接
     *
     * @param channel
     * @return
     */
    public static ServerClient addClient(NioSocketChannel channel) {
        String id = channel.id().toString();
        BrokerServerClient client = new BrokerServerClient(channel);
        ID_CLIENT.put(id, client);
        return client;
    }

    public static ServerClient delClient(NioSocketChannel channel) throws IOException {
        String id = channel.id().toString();
        ServerClient client = ID_CLIENT.remove(id);
        client.close();
        log.info("client disconnect,client is {}", client);
        return client;
    }

    /**
     * mirror 发生变化
     * @param mirrorName
     * @throws InterruptedException
     */
    public synchronized static void setMirror(String mirrorName) throws InterruptedException, IOException {
        if (StringUtils.isNotBlank(ServerClientManager.mirrorId)){
            ServerClient mirrorClient = ID_CLIENT.get(mirrorId);
            mirrorClient.close();
        }
        NettyServer server = NettyServer.getServer();
        String[] strings = StringUtils.split(mirrorName, "-");
        String[] ss = StringUtils.split(strings[0], ":");
        String hostName = ss[0];
        int port = Integer.valueOf(ss[1]);
        ServerClient client = server.connect(hostName, port);
        client.setName(mirrorName);
        client.setIsBroker(true);
        client.ready();
        Metadata metadata = new Metadata();
        MessageDesc desc = new MessageDesc();
        ProtocolBean bean = new ProtocolBean();
        bean.setGroupId(myName);
        bean.setCommand(CommonConstant.Command.BROKER_SUBJECT_REGISTER);
        List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, desc, bean);
        client.write(bytes, false);
        ServerClientManager.mirrorId = client.id();
    }

    /**
     * 由主体发送消息进行通知
     * 主体发生了变化,通知derby将之前的subject  数据存储到内存,替换subject
     * @param newSubjectId 新的subjectId
     */
    public synchronized static void subjectChange(String newSubjectId){
        String oldSubjectId = ServerClientManager.subjectId;
        if (StringUtils.isBlank(oldSubjectId)){
            //之前没有subject
            ServerClientManager.subjectId = newSubjectId;
            return ;
        }
        ServerClient client = ID_CLIENT.get(oldSubjectId);
        String oldSubjectName = client.name();
        //todo 通知derby,之前的subject 换了,把数据库中的数据读出来


    }



    public static void setMyName(String myName) {
        ServerClientManager.myName = myName;
    }

    public static ServerClient getServerClient(String id) {
        return ID_CLIENT.get(id);
    }

}
