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

    /**
     * 命令 class name
     */
    private String commandClass;

    /**
     * 命令序列化内容
     */
    private byte[] commandText;

    /**
     * 元数据序列化内容
     */
    private byte[] metadataText;

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

    public String getCommandClass() {
        return commandClass;
    }

    public void setCommandClass(String commandClass) {
        this.commandClass = commandClass;
    }

    public byte[] getCommandText() {
        return commandText;
    }

    public void setCommandText(byte[] commandText) {
        this.commandText = commandText;
    }

    public byte[] getMetadataText() {
        return metadataText;
    }

    public void setMetadataText(byte[] metadataText) {
        this.metadataText = metadataText;
    }

    @Override
    public String toString() {
        return "ProtocolBean{" +
                "dataClass='" + dataClass + '\'' +
                ", dataText=" + Arrays.toString(dataText) +
                ", commandClass='" + commandClass + '\'' +
                ", commandText=" + Arrays.toString(commandText) +
                ", metadataText=" + Arrays.toString(metadataText) +
                '}';
    }
}
