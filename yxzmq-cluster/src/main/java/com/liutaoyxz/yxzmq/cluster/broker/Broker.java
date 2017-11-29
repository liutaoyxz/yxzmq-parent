package com.liutaoyxz.yxzmq.cluster.broker;

import java.util.LinkedList;

/**
 * @author Doug Tao
 * @Date: 10:50 2017/11/29
 * @Description: cluster 中broker 对象, 保存着自己的name,主体的name和镜像的name
 *
 * 如果subject 是null,说明自己是第一个,subject就是 tail,如果同时tail 也是null,说明全局就一个broker
 *
 */
public class Broker {

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
     * 第一个
     */
    private Broker head;

    /**
     * 最后一个
     */
    private Broker tail;

    private Broker(){

    }

    public static Broker createBrokers(LinkedList<String> list){
        if (list == null || list.isEmpty()){
            return null;
        }
        if (list.size() == 1){
            //就一个broker,head 和 tail都是null
            Broker broker = new Broker();
            broker.name = list.pollFirst();
            return broker;
        }




        return null;
    }

}
