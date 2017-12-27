package com.liutaoyxz.yxzmq.io.message;

/**
 * @author Doug Tao
 * @Date: 15:25 2017/12/27
 * @Description:
 */
public class DestinationId {

    private String id;

    private MessageId messageId;

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageId getMessageId() {
        return messageId;
    }

    public void setMessageId(MessageId messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "DestinationId{" +
                "id='" + id + '\'' +
                ", messageId=" + messageId +
                '}';
    }
}
