package com.liutaoyxz.yxzmq.broker.storage;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.Group;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Doug Tao
 * @Date: 9:46 2017/11/23
 * @Description: 队列的监听任务, 每一个队列对应一个任务
 */
public class QueueListenTask implements Runnable {

    private BlockingDeque<QueueMessage> messages;

    private CopyOnWriteArrayList<Group> groups;

    private AtomicBoolean cancel;

    private final ReentrantLock lock;

    private final Condition condition;

    public static final Logger log = LoggerFactory.getLogger(QueueListenTask.class);

    public QueueListenTask(BlockingDeque<QueueMessage> messages, CopyOnWriteArrayList<Group> groups, AtomicBoolean cancel) {
        this.messages = messages;
        this.groups = groups;
        this.cancel = cancel;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    @Override
    public void run() {
        while (!cancel.get()) {
            lock.lock();
            try {
                if (groups.size() == 0) {
                    condition.await();
                    continue;
                }
                grouploop:
                for (Group g : groups) {
                    QueueMessage message = messages.take();
                    Client client = g.applyClient();
                    if (client == null){
                        if (!g.isAlive()){
                            groups.remove(g);
                        }
                        messages.addLast(message);
                        continue grouploop;
                    }else {
                        SocketChannel channel = client.channel();
                        String text = message.getText();
                        String queueName = message.getDesc().getTitle();
                        ProtocolBean bean = new ProtocolBean();
                        bean.setCommand(CommonConstant.Command.SEND);
                        bean.setDataBytes(text.getBytes(Charset.forName(ReadContainer.DEFAULT_CHARSET)));
                        MessageDesc desc = new MessageDesc();
                        desc.setType(CommonConstant.MessageType.QUEUE);
                        desc.setTitle(queueName);
                        Metadata metadata = new Metadata();
                        List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, desc, bean);
                        for (byte[] b : bytes) {
                            ByteBuffer buffer = ByteBuffer.wrap(b);
                            while (buffer.hasRemaining()) {
                                try {
                                    channel.write(buffer);
                                } catch (IOException e) {
                                    log.debug("channel cancel", e);
                                    g.delActiveClient(client);
                                }
                            }
                        }
                        g.returnClient(client);
                    }

                }
            } catch (InterruptedException e) {
                log.debug("queue task error", e);
            }finally {
                lock.unlock();
            }
        }
    }

    public Condition getCondition() {
        return condition;
    }

    public ReentrantLock getLock() {
        return lock;
    }
}
