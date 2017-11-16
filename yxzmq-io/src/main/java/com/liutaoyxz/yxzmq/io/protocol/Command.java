package com.liutaoyxz.yxzmq.io.protocol;

/**
 * Created by liutao on 2017/11/14.
 * 命令操作
 *  命令类型:
 *      生产消息
 *      消费确认
 *      收到确认
 *
 */
public interface Command {

    /**
     * 执行操作
     * @param cliendId
     * @return
     */
    boolean opeartor(String cliendId);

}
