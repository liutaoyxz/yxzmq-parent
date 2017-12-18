package com.liutaoyxz.yxzmq.broker.storage;

import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.broker.client.ServerClientManager;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Doug Tao
 * @Date: 9:46 2017/11/23
 * @Description: 队列的监听任务, 每一个队列对应一个任务
 */
public class NettyQueueListenTask implements Runnable {

    private BlockingDeque<QueueMessage> messages;

    private BlockingDeque<ServerClient> clients;

    private AtomicBoolean cancel;

    public static final Logger log = LoggerFactory.getLogger(NettyQueueListenTask.class);

    public NettyQueueListenTask(BlockingDeque<QueueMessage> messages, BlockingDeque<ServerClient> clients, AtomicBoolean cancel) {
        this.messages = messages;
        this.clients = clients;
        this.cancel = cancel;
    }

    @Override
    public void run() {
        while (!cancel.get()) {
            try {
                    ServerClient client = clients.take();
                    QueueMessage message = messages.take();
                    if (!NettyMessageContainer.checkQueueListener(message.getDesc().getTitle(),client)) {
                        //client 不可用,不管他了
                        messages.addLast(message);
                    } else {
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
                        boolean write = false;
                        try {
                            write = client.write(bytes, true);
                        }catch (Exception e){
                            log.error("write data error",e);
                            messages.addLast(message);
                            if (ServerClientManager.checkServerClient(client)){
                                clients.add(client);
                            }
                            continue;
                        }
                        if (!write){
                            //发送失败了
                            messages.addLast(message);
                            continue;
                        }
                        clients.add(client);
                    }
            } catch (InterruptedException e) {
                log.debug("queue task error", e);
            }
        }
    }

}
