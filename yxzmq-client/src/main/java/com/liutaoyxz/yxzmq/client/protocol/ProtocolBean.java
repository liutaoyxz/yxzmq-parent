package com.liutaoyxz.yxzmq.client.protocol;

import java.util.Arrays;

/**
 * Created by liutao on 2017/11/14.
 */
public class ProtocolBean {

    /**
     * 数据class name
     */
    private String DataClass;

    /**
     * 数据序列化内容
     */
    private byte[] DataText;

    /**
     * 命令 class name
     */
    private String CommandClass;

    /**
     * 命令序列化内容
     */
    private byte[] CommandText;

    /**
     * 元数据序列化内容
     */
    private byte[] metadataText;

    public String getDataClass() {
        return DataClass;
    }

    public void setDataClass(String dataClass) {
        DataClass = dataClass;
    }

    public byte[] getDataText() {
        return DataText;
    }

    public void setDataText(byte[] dataText) {
        DataText = dataText;
    }

    public String getCommandClass() {
        return CommandClass;
    }

    public void setCommandClass(String commandClass) {
        CommandClass = commandClass;
    }

    public byte[] getCommandText() {
        return CommandText;
    }

    public void setCommandText(byte[] commandText) {
        CommandText = commandText;
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
                "DataClass='" + DataClass + '\'' +
                ", DataText=" + Arrays.toString(DataText) +
                ", CommandClass='" + CommandClass + '\'' +
                ", CommandText=" + Arrays.toString(CommandText) +
                ", metadataText=" + Arrays.toString(metadataText) +
                '}';
    }
}
