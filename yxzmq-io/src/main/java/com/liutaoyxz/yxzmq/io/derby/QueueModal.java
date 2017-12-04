package com.liutaoyxz.yxzmq.io.derby;

/**
 * @author Doug Tao
 * @Date 下午11:05 2017/12/2
 * @Description:
 */
public class QueueModal {

    private Integer id;

    private String brokerName;

    private String queueId;

    private String queueName;

    private String message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public String toString() {
        return "QueueModal{" +
                "id=" + id +
                ", brokerName='" + brokerName + '\'' +
                ", queueId='" + queueId + '\'' +
                ", queueName='" + queueName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
