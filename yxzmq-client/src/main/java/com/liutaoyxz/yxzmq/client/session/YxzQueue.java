package com.liutaoyxz.yxzmq.client.session;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Doug Tao
 * @Date 下午8:24 2017/11/22
 * @Description:
 */
public class YxzQueue implements Queue {

    private String queueName;

    YxzQueue(String queueName){
        this.queueName = queueName;
    }

    @Override
    public String getQueueName() throws JMSException {
        return this.queueName;
    }
}
