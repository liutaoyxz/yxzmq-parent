package com.liutaoyxz.yxzmq.io.protocol;

/**
 * Created by liutao on 2017/11/14.
 * 元数据,轻易不要改变结构
 */
public class Metadata {
    //clientid
    private String cliendId;
    //创建连接时间
    private Long createTime;
    //数据字节数组长度
    private Integer dataLenght;
    //命令字节数组长度
    private Integer commandLenght;

    public String getCliendId() {
        return cliendId;
    }

    public void setCliendId(String cliendId) {
        this.cliendId = cliendId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getDataLenght() {
        return dataLenght;
    }

    public void setDataLenght(Integer dataLenght) {
        this.dataLenght = dataLenght;
    }

    public Integer getCommandLenght() {
        return commandLenght;
    }

    public void setCommandLenght(Integer commandLenght) {
        this.commandLenght = commandLenght;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "cliendId='" + cliendId + '\'' +
                ", createTime=" + createTime +
                ", dataLenght=" + dataLenght +
                ", commandLenght=" + commandLenght +
                '}';
    }
}
