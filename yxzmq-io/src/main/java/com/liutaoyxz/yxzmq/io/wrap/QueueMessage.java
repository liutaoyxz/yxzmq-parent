package com.liutaoyxz.yxzmq.io.wrap;

import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;

/**
 * @author Doug Tao
 * @Date ${time} ${date}
 * @Description: 队列消息保存的对象类
 */
public class QueueMessage {

    private MessageDesc desc;

    private String text;

    public MessageDesc getDesc() {
        return desc;
    }

    public void setDesc(MessageDesc desc) {
        this.desc = desc;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "QueueMessage{" +
                "desc=" + desc +
                ", text='" + text + '\'' +
                '}';
    }
}
