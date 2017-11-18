package com.liutaoyxz.yxzmq.io.Message;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * @author Doug Tao
 * @Date 下午7:30 2017/11/18
 * @Description:
 */
public class YxzTextMessage extends AbstratMessage implements TextMessage{



    @Override
    public void clearBody() throws JMSException {

    }

    @Override
    public <T> T getBody(Class<T> aClass) throws JMSException {
        return null;
    }

    @Override
    public void setText(String s) throws JMSException {

    }

    @Override
    public String getText() throws JMSException {
        return null;
    }
}
