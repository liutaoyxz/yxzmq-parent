package com.liutaoyxz.yxzmq.io.protocol;

/**
 * @author Doug Tao
 * @Date: 17:43 2017/11/15
 * @Description:
 */
public class TextMessage implements Message<String> {

    public TextMessage() {
    }

    public TextMessage(String content){
        this.content = content;
    }

    private String content;

    @Override
    public String getContent() {
        return this.content;
    }
}
