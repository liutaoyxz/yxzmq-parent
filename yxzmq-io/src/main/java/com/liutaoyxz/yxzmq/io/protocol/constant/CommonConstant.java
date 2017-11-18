package com.liutaoyxz.yxzmq.io.protocol.constant;

/**
 * @author Doug Tao
 * @Date: 16:16 2017/11/17
 * @Description:
 */
public interface CommonConstant {


    /**
     * 命令类型
     */
    interface Command {

        /**
         * 发送消息
         */
        int SEND = 1;
        /**
         * 订阅
         */
        int SUBSCRIBE = 2;

    }

    /**
     * 消息类型
     */
    interface MessageType {

        /**
         * 主题模式
         */
        int TOPIC = 1;
        /**
         * 队列模式
         */
        int QUEUE = 2;

    }

    /**
     * 消息处理标识
     */
    interface ResponseStatus {

        /**
         * 接收到
         */
        int RECEIVE = 1;

        /**
         * 处理完成
         */
        int COMPLETE = 2;

    }

//    interface





}
