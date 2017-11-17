package com.liutaoyxz.yxzmq.io.protocol;

/**
 * @author Doug Tao
 * @Date: 11:08 2017/11/17
 * @Description: 消息的描述
 */
public class MessageDesc {

    /**
     * 消息类型  topic/queue
     */
    private int type;

    /**
     * 消息主题,对应topic 或者 queue的主题
     */
    private String title;

    /**
     * 消息id
     */
    private long messageId;

    /**
     * 是否是事务消息
     */
    private boolean transaction;

    /**
     * 事务组/排序组 id
     */
    private long groupId;

    /**
     * 是否是顺序消息
     */
    private boolean order;

    /**
     * 事务/顺序组中的索引
     */
    private int index;

    /**
     * 事务/顺序组中的总数
     */
    private int total;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public void setTransaction(boolean transaction) {
        this.transaction = transaction;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean order) {
        this.order = order;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "MessageDesc{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", messageId=" + messageId +
                ", transaction=" + transaction +
                ", groupId=" + groupId +
                ", order=" + order +
                ", index=" + index +
                ", total=" + total +
                '}';
    }
}
