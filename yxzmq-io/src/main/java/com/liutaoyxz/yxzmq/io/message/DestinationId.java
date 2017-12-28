package com.liutaoyxz.yxzmq.io.message;

/**
 * @author Doug Tao
 * @Date: 15:25 2017/12/27
 * @Description:
 */
public class DestinationId {

    private String id;

    private SessionId sessionId;

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "DestinationId{" +
                "id='" + id + '\'' +
                ", sessionId=" + sessionId +
                '}';
    }
}
