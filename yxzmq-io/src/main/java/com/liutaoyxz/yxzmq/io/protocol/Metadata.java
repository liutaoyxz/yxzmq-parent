package com.liutaoyxz.yxzmq.io.protocol;

/**
 * Created by liutao on 2017/11/14.
 * 元数据,轻易不要改变结构
 * @author Doug Tao
 */
public class Metadata {



    /**
     * clientId
     */
    private String clientId;
    /**
     * 创建时间,时间戳
     */
    private Long createTime;
    /**
     * protocolBean 的序列化数据长度
     */
    private Integer beanSize;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
                "clientId='" + clientId + '\'' +
                ", createTime=" + createTime +
                ", beanSize=" + beanSize +
                '}';
    }
}
