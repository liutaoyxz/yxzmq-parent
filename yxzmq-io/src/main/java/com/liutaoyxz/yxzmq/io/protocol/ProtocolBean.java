package com.liutaoyxz.yxzmq.io.protocol;

import java.util.Arrays;

/**
 * @author Doug Tao
 */
public class ProtocolBean {

    /**
     * 命令
     */
    private int command;
    /**
     * connection 的groupId
     */
    private String groupId;

    /**
     * 消息描述类 class name
     */
    private String descClass;

    /**
     * 消息描述对象  bytes
     */
    private byte[] descBytes;

    /**
     * 数据class name
     */
    private String dataClass;

    /**
     * 数据序列化内容
     */
    private byte[] dataBytes;

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public String getDescClass() {
        return descClass;
    }

    public void setDescClass(String descClass) {
        this.descClass = descClass;
    }

    public byte[] getDescBytes() {
        return descBytes;
    }

    public void setDescBytes(byte[] descBytes) {
        this.descBytes = descBytes;
    }

    public String getDataClass() {
        return dataClass;
    }

    public void setDataClass(String dataClass) {
        this.dataClass = dataClass;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "ProtocolBean{" +
                "command=" + command +
                ", groupId='" + groupId + '\'' +
                ", descClass='" + descClass + '\'' +
                ", descBytes=" + Arrays.toString(descBytes) +
                ", dataClass='" + dataClass + '\'' +
                ", dataBytes=" + Arrays.toString(dataBytes) +
                '}';
    }
}
