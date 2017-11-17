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
    static interface Command {

        /**
         * 发送消息
         */
        static final int SEND = 1;

    }

    /**
     * 消息类型
     */
    static interface MessageType {

        /**
         * 主题模式
         */
        static final int TOPIC = 1;
        /**
         * 队列模式
         */
        static final int QUEUE = 2;

    }

    /**
     * 消息处理标识
     */
    static interface SendStatus {

        /**
         * 接收到
         */
        static final int RECEIVE = 1;

        /**
         * 处理完成
         */
        static final int COMPLETE = 2;



    }


}
