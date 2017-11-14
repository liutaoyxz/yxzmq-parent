package com.liutaoyxz.yxzmq.client.protocol;

/**
 * Created by liutao on 2017/11/14.
 * 命令操作
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
