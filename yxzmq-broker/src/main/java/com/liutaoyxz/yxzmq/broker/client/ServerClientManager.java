package com.liutaoyxz.yxzmq.broker.client;

import com.liutaoyxz.yxzmq.broker.server.Server;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * 增加一个连接
     * @param channel
     * @return
     */
    public static ServerClient addClient(NioSocketChannel channel){
        String id = channel.id().toString();
        BrokerServerClient client = new BrokerServerClient(channel);
        ID_CLIENT.put(id,client);
        return client;
    }

    public static ServerClient delClient(NioSocketChannel channel){
        String id = channel.id().toString();
        ServerClient client = ID_CLIENT.remove(id);
        log.info("client disconnect,client is {}", client);
        return client;
    }

    public static ServerClient addBrokerClient(NioSocketChannel channel){
        String id = channel.id().toString();
        ServerClient client = new BrokerServerClient(channel,true);
        ID_CLIENT.put(id,client);
        log.info("new client connect,client is {}",client);
        return client;
    }

}
