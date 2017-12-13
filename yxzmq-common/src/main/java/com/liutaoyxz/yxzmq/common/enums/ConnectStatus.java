package com.liutaoyxz.yxzmq.common.enums;

/**
 * @author Doug Tao
 * @Date: 16:45 2017/12/13
 * @Description:
 */
public enum ConnectStatus {
    /** 还未连接 **/
    NOT_CONNECT(0),

    /** 连接中 **/
    CONNECTING(1);


    private int code;

    ConnectStatus(int code) {
        this.code = code;
    }


}
