package com.liutaoyxz.yxzmq.broker;

import com.liutaoyxz.yxzmq.broker.Client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Doug Tao
 * @Date: 13:44 2017/11/20
 * @Description: 一个clientGroup   一个group 代表 终端的一个connection
 *
 */
public class Group {

    private static final AtomicInteger AUTO_INCREASE_GROUP_ID = new AtomicInteger(1);

    /**
     * 这一组的id 一个groupid 代表客户端唯一一个connection
     */
    private String groupId;

    /**
     * 辅助client
     */
    private Client assistClient;

    /**
     * 主client
     */
    private BlockingQueue<Client> clients;


    public Group(Client assistClient) {
        this.assistClient = assistClient;
        this.clients = new LinkedBlockingQueue<>();
    }

    public Client assistClient(){
        return this.assistClient;
    }

    public String groupId(){
        return this.groupId;
    }

    public void setGroupId(String groupId){
        this.groupId = groupId;
    }

    public static String nextGroupId(){
        return "yxzmq-broker-group-"+AUTO_INCREASE_GROUP_ID.getAndIncrement();
    }


}
