package com.liutaoyxz.yxzmq.io.wrap;

import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;

/**
 * @author Doug Tao
 * @Date 上午7:26 2017/11/18
 * @Description: 主题消息包装类
 */
public class TopicMessage {
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
        return "TopicMessage{" +
                "desc=" + desc +
                ", text='" + text + '\'' +
                '}';
    }
}
