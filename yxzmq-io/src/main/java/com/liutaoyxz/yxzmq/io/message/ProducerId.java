package com.liutaoyxz.yxzmq.io.message;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Doug Tao
 * @Date: 15:06 2017/12/21
 * @Description:
 */
public class ProducerId implements Serializable{

    private static final long serialVersionUID = -7279146008612654366L;
    private SessionId sessionId;

    private String id;

    public ProducerId() {
    }

    public ProducerId(SessionId sessionId, String id) {
        if (sessionId == null || StringUtils.isBlank(id)){
            throw new IllegalArgumentException();
        }
        this.sessionId = sessionId;
        this.id = id;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ProducerId{" +
                "sessionId=" + sessionId +
                ", id='" + id + '\'' +
                '}';
    }
}
