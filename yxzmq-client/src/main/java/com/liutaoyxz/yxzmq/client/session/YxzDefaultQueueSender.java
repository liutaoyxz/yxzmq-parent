package com.liutaoyxz.yxzmq.client.session;

import javax.jms.*;

/**
 * @author Doug Tao
 * @Date: 13:15 2017/11/23
 * @Description:
 */
public class YxzDefaultQueueSender extends AbstractProducer implements QueueSender{

    private Queue queue;

    private YxzDefaultSession session;

    YxzDefaultQueueSender(YxzDefaultSession session,Queue queue){
        this.queue = queue;
        this.session = session;
    }

    @Override
    public void send(Message message) throws JMSException {
        YxzSessionTask task = new YxzSessionTask(this.session,YxzSessionTask.QUEUE_SEND);
        task.setMessage((TextMessage) message);
        task.setQueue(this.queue);
        this.session.addTask(task);
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
}
