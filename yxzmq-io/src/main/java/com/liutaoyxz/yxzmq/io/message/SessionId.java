package com.liutaoyxz.yxzmq.io.message;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Doug Tao
 * @Date: 15:56 2017/12/21
 * @Description:
 */
public class SessionId implements Serializable{

    private static final long serialVersionUID = -5510145130880166236L;
    private ConnectionId connectionId;

    private String id;


    public SessionId(ConnectionId connectionId, String id) {
        if (connectionId == null || StringUtils.isBlank(id)){
            throw new IllegalArgumentException();
        }
        this.connectionId = connectionId;
        this.id = id;
    }

    public SessionId() {
    }

    public ConnectionId getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SessionId{" +
                "connectionId=" + connectionId +
                ", id='" + id + '\'' +
                '}';
    }
}
