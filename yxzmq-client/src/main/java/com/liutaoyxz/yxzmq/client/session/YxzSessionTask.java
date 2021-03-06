package com.liutaoyxz.yxzmq.client.session;

import com.liutaoyxz.yxzmq.client.connection.YxzConnectionTask;
import com.liutaoyxz.yxzmq.client.connection.YxzDefaultConnection;
import com.liutaoyxz.yxzmq.common.util.ProtostuffUtil;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Doug Tao
 * @Date 下午1:16 2017/11/19
 * @Description:
 */
public class YxzSessionTask implements Runnable {

    public static final Logger log = LoggerFactory.getLogger(YxzSessionTask.class);

    /**
     * 发布消息
     */
    static final int PUBLISH = 1;

    /**
     * 订阅主题
     */
    static final int SUBSCRIBE = 2;

    /**
     * 监听queue
     */
    static final int LISTEN_QUEUE = 3;

    static final int QUEUE_SEND = 4;


    private Topic topic;

    private Queue queue;

    private TextMessage message;

    private int type;


    private YxzDefaultSession session;

    public YxzSessionTask(YxzDefaultSession session,int type) {
        this.session = session;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            List<byte[]> bytes = createProtocolBytes();
            YxzDefaultConnection connection = this.session.getConnection();
            YxzConnectionTask connectionTask = new YxzConnectionTask(connection,YxzConnectionTask.SEND_DATA);
            connectionTask.setData(bytes);
            connection.addTask(connectionTask);
        } catch (JMSException e) {
            log.debug("createProtocolBytes error",e);
            e.printStackTrace();
        }
    }


    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public TextMessage getMessage() {
        return message;
    }

    public void setMessage(TextMessage message) {
        this.message = message;
    }

    /**
     * 创建协议数据
     * @return
     */
    private List<byte[]> createProtocolBytes() throws JMSException{
        List<byte[]> result = null;
        Metadata metadata = null;
        ProtocolBean bean = null;
        MessageDesc desc = null;
        String text = null;
        switch (this.type){
            case PUBLISH:
                result = new ArrayList<>();
                //发布消息
                desc = new MessageDesc();
                desc.setType(CommonConstant.MessageType.TOPIC);
                desc.setTitle(topic.getTopicName());
                byte[] descBytes = ProtostuffUtil.serializable(desc);

                bean = new ProtocolBean();
                bean.setDescBytes(descBytes);
                bean.setDescClass(MessageDesc.class.getName());

                text = message.getText();
                bean.setDataBytes(text.getBytes(Charset.forName(ReadContainer.DEFAULT_CHARSET)));
                bean.setCommand(CommonConstant.Command.SEND);
                byte[] beanBytes = ProtostuffUtil.serializable(bean);

                metadata = new Metadata();
                metadata.setCreateTime(message.getJMSTimestamp());
                metadata.setBeanSize(beanBytes.length);

                byte[] metadataBytes = ProtostuffUtil.serializable(metadata);
                int metadataLenght = metadataBytes.length;

                byte[] lenght = ProtostuffUtil.fillMetadataLength(metadataLenght).getBytes(Charset.forName(ReadContainer.DEFAULT_CHARSET));
                result.add(lenght);
                result.add(metadataBytes);
                result.add(beanBytes);
                break;
            case SUBSCRIBE:
                bean = new ProtocolBean();
                bean.setCommand(CommonConstant.Command.SUBSCRIBE);
                desc = new MessageDesc();
                desc.setTitle(topic.getTopicName());
                desc.setType(CommonConstant.MessageType.TOPIC);
                metadata = new Metadata();
                result = BeanUtil.convertBeanToByte(metadata, desc, bean);
                break;

            case LISTEN_QUEUE:
                bean = new ProtocolBean();
                bean.setCommand(CommonConstant.Command.QUEUE_LISTEN);
                desc = new MessageDesc();
                desc.setTitle(queue.getQueueName());
                desc.setType(CommonConstant.MessageType.QUEUE);
                metadata = new Metadata();
                result = BeanUtil.convertBeanToByte(metadata, desc, bean);
                break;

            case QUEUE_SEND:
                bean = new ProtocolBean();
                bean.setCommand(CommonConstant.Command.SEND);
                text = message.getText();
                bean.setDataBytes(text.getBytes(Charset.forName(ReadContainer.DEFAULT_CHARSET)));
                desc = new MessageDesc();
                desc.setTitle(queue.getQueueName());
                desc.setType(CommonConstant.MessageType.QUEUE);
                metadata = new Metadata();
                result = BeanUtil.convertBeanToByte(metadata, desc, bean);
                break;
            default:
                break;
        }
        return result;
    }

}
