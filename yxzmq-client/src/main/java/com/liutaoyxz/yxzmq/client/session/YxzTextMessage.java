package com.liutaoyxz.yxzmq.client.session;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * @author Doug Tao
 * @Date 下午7:30 2017/11/18
 * @Description:
 */
public class YxzTextMessage extends AbstratMessage implements TextMessage{


    private String text;

    YxzTextMessage(String text){
        try {
            this.setJMSTimestamp(System.currentTimeMillis());
        } catch (JMSException e) {
            e.printStackTrace();
        }
        this.text = text;
    }

    @Override
    public void clearBody() throws JMSException {

    }

    @Override
    public <T> T getBody(Class<T> aClass) throws JMSException {
        return null;
    }

    @Override
    public void setText(String text) throws JMSException {
        this.text = text;
    }

    @Override
    public String getText() throws JMSException {
        return this.text;
    }
}
