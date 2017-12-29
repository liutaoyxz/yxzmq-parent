package com.liutaoyxz.yxzmq.io.message;

import org.apache.commons.lang3.StringUtils;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * @author Doug Tao
 * @Date: 12:57 2017/12/25
 * @Description:
 */
public class YxzTextMessage extends AbstractYxzMessage implements TextMessage {


    private String text;


    public YxzTextMessage(String text) {
        super(AbstractYxzMessage.TEXT_MESSAGE);
        if (StringUtils.isBlank(text)){
            throw new IllegalArgumentException("text is blank");
        }
        this.text = text;
    }

    public YxzTextMessage() {
        super(AbstractYxzMessage.TEXT_MESSAGE);
    }

    @Override
    public void setText(String text) throws JMSException {
        checkWritable();
        if (StringUtils.isBlank(text)){
            throw new IllegalArgumentException("text is blank");
        }
        this.text = text;
    }

    @Override
    public String getText() throws JMSException {
        return this.text;
    }
}
