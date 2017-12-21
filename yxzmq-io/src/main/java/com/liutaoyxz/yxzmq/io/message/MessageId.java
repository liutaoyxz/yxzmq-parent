package com.liutaoyxz.yxzmq.io.message;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Doug Tao
 * @Date: 16:23 2017/12/21
 * @Description:
 */
public class MessageId implements Serializable{

    private static final long serialVersionUID = -8677376309149642560L;
    private ProducerId producerId;

    private String id;

    public MessageId() {
    }

    public MessageId(ProducerId producerId, String id) {
        if (producerId == null || StringUtils.isBlank(id)){
            throw new IllegalArgumentException();
        }
        this.producerId = producerId;
        this.id = id;
    }

    public ProducerId getProducerId() {
        return producerId;
    }

    public void setProducerId(ProducerId producerId) {
        this.producerId = producerId;
    }

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MessageId{" +
                "producerId=" + producerId +
                ", id='" + id + '\'' +
                '}';
    }
}
