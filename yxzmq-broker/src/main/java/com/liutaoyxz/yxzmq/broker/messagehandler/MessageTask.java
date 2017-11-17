package com.liutaoyxz.yxzmq.broker.messagehandler;

/**
 * @author Doug Tao
 * @Date: 17:13 2017/11/17
 * @Description: 消息任务处理
 */
public interface MessageTask {

    /**
     * 启动有关消息处理的所有任务
     * @return
     */
    boolean start();

    /**
     * 停止
     * @return
     */
    boolean stop();

    /**
     * 重启,不一定用得上
     * @return
     */
    boolean restart();

    /**
     * 检查,是否正常运行
     * @return
     */
    boolean check();


}
