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
    //protocolBean 序列化的长度
    private Integer beanSize;

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

    public Integer getBeanSize() {
        return beanSize;
    }

    public void setBeanSize(Integer beanSize) {
        this.beanSize = beanSize;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "cliendId='" + cliendId + '\'' +
                ", createTime=" + createTime +
                ", beanSize=" + beanSize +
                '}';
    }
}
