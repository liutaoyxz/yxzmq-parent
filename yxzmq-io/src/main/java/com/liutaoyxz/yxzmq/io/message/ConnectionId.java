package com.liutaoyxz.yxzmq.io.message;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Doug Tao
 * @Date: 15:11 2017/12/21
 * @Description:
 */
public class ConnectionId implements Serializable{

    private static final long serialVersionUID = 7469344394133910503L;
    private String id;

    public ConnectionId(String id) {
        if (StringUtils.isBlank(id)){
            throw new IllegalArgumentException();
        }
        this.id = id;
    }

    public ConnectionId() {
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ConnectionId{" +
                "id='" + id + '\'' +
                '}';
    }
}
