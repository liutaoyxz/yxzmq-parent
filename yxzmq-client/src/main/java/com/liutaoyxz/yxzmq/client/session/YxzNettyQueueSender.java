package com.liutaoyxz.yxzmq.client.session;

import com.liutaoyxz.yxzmq.client.YxzClientContext;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 13:15 2017/11/23
 * @Description:
 */
public class YxzNettyQueueSender extends AbstractProducer implements QueueSender{

    private Queue queue;

    private YxzClientContext ctx;

    YxzNettyQueueSender(Queue queue,YxzClientContext ctx){
        this.queue = queue;
        this.ctx = ctx;
    }

    @Override
    public void send(Message message) throws JMSException {
        checkMessage(message);
        SessionTask task = new SessionTask(SessionTask.QUEUE_SEND);
        task.setMessage(message);
        task.setQueue(this.queue);
        ctx.session().addTask(task);
    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {

    }

    @Override
    public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {

    }

    @Override
    public Queue getQueue() throws JMSException {
        return this.queue;
    }

    @Override
    public void send(Queue queue, Message message) throws JMSException {

    }

    @Override
    public void send(Queue queue, Message message, int i, int i1, long l) throws JMSException {

    }

    private void checkMessage(Message msg){
        if (msg == null) {
            throw new NullPointerException();
        }
    }

}
