package com.liutaoyxz.yxzmq.io.protocol;

import java.util.Arrays;

/**
 * Created by liutao on 2017/11/14.
 */
public class ProtocolBean {

    /**
     * 数据class name
     */
    private String dataClass;

    /**
     * 数据序列化内容
     */
    private byte[] dataText;


    public String getDataClass() {
        return dataClass;
    }

    public void setDataClass(String dataClass) {
        this.dataClass = dataClass;
    }

    public byte[] getDataText() {
        return dataText;
    }

    public void setDataText(byte[] dataText) {
        this.dataText = dataText;
    }

    @Override
    public String toString() {
        return "ProtocolBean{" +
                "dataClass='" + dataClass + '\'' +
                ", dataText=" + Arrays.toString(dataText) +
                '}';
    }
}
