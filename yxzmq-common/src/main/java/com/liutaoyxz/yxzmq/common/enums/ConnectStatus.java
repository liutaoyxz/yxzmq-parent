package com.liutaoyxz.yxzmq.common.enums;

/**
 * @author Doug Tao
 * @Date: 16:45 2017/12/13
 * @Description:
 */
public enum ConnectStatus {
    /** 还未连接 **/
    NOT_CONNECT(0),

    /** 已连接,未注册 **/
    NOT_REGISTER(1),

    /** 已连接,已注册 **/
    REGISTERED(2),

    /**连接断开**/
    DISCONNECTED(3),

    /**  连接关闭 **/
    CLOSED(-1)

    ;


    private int code;

    ConnectStatus(int code) {
        this.code = code;
    }


}
