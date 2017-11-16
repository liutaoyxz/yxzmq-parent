package com.liutaoyxz.yxzmq.io.protocol;

/**
 * Created by liutao on 2017/11/14.
 * 命令操作
 *  命令类型:
 *      生产消息
 *      消费确认
 *      收到确认
 *  消息:
 *      消息描述部分,描述消息的类型,p2p还是topic,具体的标识
 *      消息体部分,具体的消息内容
 */
public interface Command {

    /**
     * 执行操作
     * @param cliendId
     * @return
     */
    boolean opeartor(String cliendId);

}
