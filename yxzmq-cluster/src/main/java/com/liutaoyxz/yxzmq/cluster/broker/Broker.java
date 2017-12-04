package com.liutaoyxz.yxzmq.cluster.broker;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Doug Tao
 * @Date: 10:50 2017/11/29
 * @Description: cluster 中broker 对象, 保存着自己的name,主体的name和镜像的name
 *
 * 如果subject 是null,说明自己是第一个,subject就是 tail,如果同时tail 也是null,说明全局就一个broker
 *
 */
public class Broker implements Comparable<Broker>{

    /**
     * 自己的name
     */
    private String name;

    /**
     * 镜像
     */
    private Broker mirror;

    /**
     * 主体
     */
    private Broker subject;

    /**
     * broker数量
     */
    private int size;

    private volatile boolean ready = false;

    public Broker(String name){
        this.name = name;
    }

    /**
     * 创建broker列表,返回本机的broker
     * @param list
     * @return
     */
    public static Broker createBrokers(List<String> list){
        if (list == null || list.isEmpty()){
            return null;
        }

        return null;
    }

    @Override
    public int compareTo(Broker other) {
        String thisName = this.name;
        String thatName = other.getName();
        int thisSqu = Integer.valueOf(thisName.split("-")[1]);
        int thatSqu = Integer.valueOf(thatName.split("-")[1]);
        return thisSqu - thatSqu;
    }

    public String getName() {
        return name;
    }

    public void ready(){
        this.ready = true;
    }

    public boolean isReady(){
        return this.ready;
    }



}
