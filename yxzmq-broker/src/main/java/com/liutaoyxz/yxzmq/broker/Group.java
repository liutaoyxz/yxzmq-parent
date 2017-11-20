package com.liutaoyxz.yxzmq.broker;

import com.liutaoyxz.yxzmq.broker.Client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Doug Tao
 * @Date: 13:44 2017/11/20
 * @Description: 一个clientGroup   一个group 代表 终端的一个connection
 *
 */
public class Group {

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

}
