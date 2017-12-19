package com.liutaoyxz.yxzmq.broker.client;

import com.liutaoyxz.yxzmq.broker.server.NettyServer;
import com.liutaoyxz.yxzmq.broker.storage.NettyMessageContainer;
import com.liutaoyxz.yxzmq.common.Address;
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
import java.util.ArrayList;
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

    /**
     * name  和 serverClient 对应的映射(注册过的channel 才会有name)
     */
    private static final ConcurrentHashMap<String, ServerClient> NAME_CLIENT = new ConcurrentHashMap<>();

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
        String name = client.name();
        if (StringUtils.isNotBlank(name)){
            NAME_CLIENT.remove(name);
        }
        client.close();
        log.info("client disconnect,client is {}", client);
        return client;
    }

    /**
     * mirror 发生变化
     *
     * @param mirrorName
     * @throws InterruptedException
     */
    public synchronized static void setMirror(String mirrorName) throws InterruptedException, IOException {
        NettyServer server = NettyServer.getServer();
        Address address = Address.createAddress(mirrorName);
        ServerClient client = server.connect(address);
        client.setName(mirrorName);
        client.setIsBroker(true);
        client.ready();
        Metadata metadata = new Metadata();
        MessageDesc desc = new MessageDesc();
        ProtocolBean bean = new ProtocolBean();
        bean.setZkName(myName);
        bean.setCommand(CommonConstant.Command.BROKER_SUBJECT_REGISTER);
        List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, desc, bean);
        client.write(bytes, false);
        ServerClientManager.mirrorId = client.id();
    }

    /**
     * 由主体发送消息进行通知
     * 主体发生了变化,通知derby将之前的subject  数据存储到内存,替换subject
     *
     * @param newSubjectId 新的subjectId
     */
    public synchronized static void subjectChange(String newSubjectId, String subjectName) throws IOException {
        ServerClient newClient = ID_CLIENT.get(newSubjectId);
        newClient.setName(subjectName);
        NAME_CLIENT.put(subjectName, newClient);
        log.info("subject change,new subject is {}", newClient);
        String oldSubjectId = ServerClientManager.subjectId;
        if (StringUtils.isBlank(oldSubjectId)) {
            //之前没有subject
            ServerClientManager.subjectId = newSubjectId;
            return;
        }

        ServerClientManager.subjectId = newSubjectId;
        ServerClient client = ID_CLIENT.get(oldSubjectId);
        if (client != null) {
            client.close();
            String oldSubjectName = client.name();
            if (subjectName.equals(oldSubjectName)) {
                //同一台 broker,不用读数据
                return;
            }
            //todo 通知derby,之前的subject 换了,把数据库中的数据读出来
            log.info("notify derby read old subject [{}] data to memory", client);


        }
    }

    /**
     * 根据name 列表查询 ServerClient  列表
     *
     * @param clients
     * @return
     */
    public static List<ServerClient> getServerClients(List<String> clients) {
        List<ServerClient> result = new ArrayList<>();
        for (String s : clients) {
            ServerClient client = NAME_CLIENT.get(s);
            if (client != null) {
                result.add(client);
            }
        }
        return result;
    }

    public static ServerClient getServerClientByName(String name){
        if (StringUtils.isBlank(name)){
            throw new NullPointerException();
        }
        return NAME_CLIENT.get(name);
    }

    public static boolean checkServerClient(ServerClient client){
        if (client != null){
            if (!client.available()){
                ID_CLIENT.remove(client.id());
                if (StringUtils.isNotBlank(client.name())){
                    NAME_CLIENT.remove(client.name());
                }
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 客户端注册
     *
     * @param id
     * @param zkName
     * @throws InterruptedException
     */
    public static void clientRegister(String id, String zkName) {
        ServerClient client = ID_CLIENT.get(id);
        if (client == null) {
            log.error("no client,id is {},zkName is {}", id, zkName);
        }
        client.setName(zkName);
        Metadata metadata = new Metadata();
        MessageDesc desc = new MessageDesc();
        ProtocolBean bean = new ProtocolBean();
        bean.setCommand(CommonConstant.Command.REGISTER_SUCCESS);
        List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, desc, bean);
        try {
            client.write(bytes, false);
            client.ready();
            NAME_CLIENT.put(zkName, client);
            NettyMessageContainer.clientReady(zkName);
        } catch (InterruptedException e) {
            log.error("client register error", e);
            //发送消息失败,什么也不做
        }
    }


    public static void setMyName(String myName) {
        ServerClientManager.myName = myName;
    }

    public static ServerClient getServerClient(String id) {
        return ID_CLIENT.get(id);
    }

}
