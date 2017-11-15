package com.liutaoyxz.yxzmq.io.protocol;

/**
 * Created by liutao on 2017/11/14.
 */
public class DefaultStringData implements BaseData {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContent() {
        return text;
    }

    @Override
    public String toString() {
        return "DefaultStringData{" +
                "text='" + text + '\'' +
                '}';
    }
}
