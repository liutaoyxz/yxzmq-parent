package com.liutaoyxz.yxzmq.common.exception;

import java.util.concurrent.Executors;

/**
 * @author Doug Tao
 * @Date: 15:00 2017/11/17
 * @Description:
 */
public class ConnectCancelException extends Exception {

    public ConnectCancelException() {
        super();
    }

    public ConnectCancelException(String message) {
        super(message);
    }

    public ConnectCancelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectCancelException(Throwable cause) {
        super(cause);
    }

    protected ConnectCancelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
