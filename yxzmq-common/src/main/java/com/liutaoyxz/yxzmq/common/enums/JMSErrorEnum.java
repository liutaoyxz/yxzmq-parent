package com.liutaoyxz.yxzmq.common.enums;

import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Doug Tao
 * @Date 上午12:32 2017/11/19
 * @Description:
 */
public enum JMSErrorEnum {

    CONNECT_ERROR("0001","connect error"),
    CONNECTION_NOT_INIT("0002","connection not init"),
    CHANNEL_OPEN_ERROR("0003","error when channel open"),
    SESSION_CLOSED("0004","session already closed"),
    CONNECTION_NOT_START("0005","connection not start"),
    QUEUE_NOT_DEFINE("0006","queue not define"),
    ;





    private static final Map<String,JMSErrorEnum> map = new HashMap<>();

    static {
        JMSErrorEnum[] enums = values();
        for (JMSErrorEnum e : enums){
            map.put(e.code(),e);
        }
    }

    private String errorCode;

    private String reason;

    public String code() {
        return errorCode;
    }


    public String reason() {
        return reason;
    }

    JMSErrorEnum(String errorCode, String reason) {
        this.errorCode = errorCode;
        this.reason = reason;
    }

    public static JMSErrorEnum get(String errorCode){
        return map.get(errorCode);
    }

    public JMSException exception(){
        return new JMSException(reason(),code());
    }

    public JMSException exception(Exception e){
        JMSException jmsException = new JMSException(reason(), code());
        jmsException.setLinkedException(e);
        return jmsException;
    }


}
