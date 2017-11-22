package com.liutaoyxz.yxzmq.broker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 13:44 2017/11/20
 * @Description: 一个clientGroup   一个group 代表 终端的一个connection
 *
 */
public class Group {

    private static final AtomicInteger AUTO_INCREASE_GROUP_ID = new AtomicInteger(1);

    public static final Logger log = LoggerFactory.getLogger(Group.class);

    /**
     * 这一组的id 一个groupid 代表客户端唯一一个connection
     */
    private String groupId;

    /**
     * 辅助client
     */
    private Client assistClient;

    private volatile boolean alive = false;

    /**
     * 主client
     */
    private BlockingQueue<Client> clients;

    private AtomicInteger clientNum = new AtomicInteger(0);

    private ReentrantLock clientLock = new ReentrantLock();

    private CopyOnWriteArrayList<String> topics = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<String> queues = new CopyOnWriteArrayList<>();


    public Group(Client assistClient) {
        this.assistClient = assistClient;
        this.clients = new LinkedBlockingQueue<>();
    }

    public Client assistClient(){
        return this.assistClient;
    }

    public void setAssistClient(Client assistClient){
        this.assistClient = assistClient;
    }

    public void addActiveClient(Client client){
        clientLock.lock();
        try {
            this.clients.add(client);
            clientNum.getAndIncrement();
            this.alive = true;
        }finally {
            clientLock.unlock();
        }
    }

    public Client applyClient(){
        try {
            if (clientNum.get() == 0){
                return null;
            }
            return clients.take();
        } catch (InterruptedException e) {
            log.debug("applyClient error",e);
        }
        return null;
    }

    /**
     * 添加订阅的主题
     */
    public void addTopic(String topicName){
        this.topics.addIfAbsent(topicName);
    }

    /**
     * 添加订阅的队列
     */
    public void addQueue(String queueName){
        this.queues.addIfAbsent(queueName);
    }

    /**
     * 删除订阅的主题
     */
    public void delTopic(String topicName){
        this.topics.remove(topicName);
    }

    /**
     * 删除订阅的队列
     */
    public void delQueue(String queueName){
        this.queues.remove(queueName);
    }

    public void delActiveClient(Client client){
        clientLock.lock();
        try {
            this.clients.remove(client);
            int i = clientNum.decrementAndGet();
            if (i == 0){
                this.alive = false;
            }
        }finally {
            clientLock.unlock();
        }
    }

    public boolean isAlive(){
        return this.alive;
    }

    public void returnClient(Client client){
        clients.add(client);
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
