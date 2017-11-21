package com.liutaoyxz.yxzmq.broker.messagehandler;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.Group;
import com.liutaoyxz.yxzmq.broker.storage.MessageContainer;
import com.liutaoyxz.yxzmq.common.util.ProtostuffUtil;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.util.BeanUtil;
import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import com.liutaoyxz.yxzmq.io.wrap.TopicMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Doug Tao
 * @Date: 17:04 2017/11/17
 * @Description: 消息处理,接收消息,根据消息类型进行不同的处理
 */
public class YxzMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(YxzMessageHandler.class);

    public static void handlerProtocolBean(ProtocolBean bean, Client client) throws IOException {     //package-private

        if (!checkBean(bean)){
            return;
        }
        final int command = bean.getCommand();
        final MessageDesc desc = getDesc(bean);
        final String msg = getMessage(bean);
        switch (command){
            case CommonConstant.Command.SEND:
                //发送过来的消息
                handlerMessage(desc,msg);
                break;
            case CommonConstant.Command.ASSIST_REGISTER:
                handlerAssistRegister(bean,client);
                break;
            case CommonConstant.Command.MAIN_REGISTER:
                handlerMainRegister(bean,client);
                break;
            default:
                log.debug("handler protocolBean error,command is {}",command);
                break;
        }

    }


    private static boolean checkBean(ProtocolBean bean){
        if (bean == null) return false;
        return true;
    }

    /**
     * 反序列化 MessageDesc
     * @param bean ProtocolBean
     * @return null 或者 desc 实例
     */
    private static MessageDesc getDesc(ProtocolBean bean){
        if (StringUtils.isBlank(bean.getDescClass()) || bean.getDescBytes() == null){
            return null;
        }
        return (MessageDesc) ProtostuffUtil.get(bean.getDescBytes(),bean.getDescClass());
    }

    /**
     * 反序列化 Message
     * @param bean ProtocolBean
     * @return null 或者 消息字符串
     */
    private static String getMessage(ProtocolBean bean){
        if (bean.getDataBytes() == null){
            return null;
        }

        return new String(bean.getDataBytes(), Charset.forName(ReadContainer.DEFAULT_CHARSET));
    }

    /**
     * 处理消息,客户端发送过来的
     * @param desc
     * @param msg
     */
    private static void handlerMessage(MessageDesc desc,String msg){
        int type = desc.getType();
        String title = desc.getTitle();
        boolean result = false;
        switch (type){
            case CommonConstant.MessageType.TOPIC:
                TopicMessage tm = new TopicMessage();
                tm.setDesc(desc);
                tm.setText(msg);
                result = MessageContainer.save(title, tm);
                log.debug("save topic message msg,result is {}",result);
                break;

            case CommonConstant.MessageType.QUEUE:
                QueueMessage qm = new QueueMessage();
                qm.setDesc(desc);
                qm.setText(msg);
                result = MessageContainer.save(title, qm);
                log.debug("save queue message msg,result is {}",result);
                break;
            default:
                log.debug("handler message error,message title is {}",title);
                break;
        }
    }

    /**
     * 客户端注册channel 注册,需要写回去groupId
     * @param bean
     * @param client
     */
    private static void handlerAssistRegister(ProtocolBean bean,Client client) throws IOException {
        String groupId = bean.getGroupId();
        SocketChannel channel = client.channel();
        if (StringUtils.isBlank(groupId)){
            //第一次注册
            Group group = new Group(client);
            group.setGroupId(Group.nextGroupId());
            client.handler().addGroup(group);
            Metadata metadata = new Metadata();
            bean  = new ProtocolBean();
            bean.setCommand(CommonConstant.Command.REGISTER_SUCCESS);
            bean.setGroupId(group.groupId());
            List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, null, bean);
            for (byte[] b:bytes){
                ByteBuffer buffer = ByteBuffer.wrap(b);
                channel.write(buffer);
                while (buffer.hasRemaining()){
                    channel.write(buffer);
                }
            }

        }else {
            //不是第一次,删掉之前的,保存现在的
            Group group = client.handler().getGroup(groupId);
            group.setGroupId(Group.nextGroupId());
            group.setAssistClient(client);
            Metadata metadata = new Metadata();
            bean  = new ProtocolBean();
            bean.setGroupId(group.groupId());
            bean.setCommand(CommonConstant.Command.REGISTER_SUCCESS);
            List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, null, bean);
            for (byte[] b:bytes){
                ByteBuffer buffer = ByteBuffer.wrap(b);
                channel.write(buffer);
                while (buffer.hasRemaining()){
                    channel.write(buffer);
                }
            }
        }
    }

    private static void handlerMainRegister(ProtocolBean bean,Client client) throws IOException {
        String groupId = bean.getGroupId();
        SocketChannel channel = client.channel();
        if (StringUtils.isBlank(groupId)){
            //有问题

        }else {
            //注册
            Group group = client.handler().getGroup(groupId);
            group.addActiveClient(client);
            Metadata metadata = new Metadata();
            bean  = new ProtocolBean();
            bean.setGroupId(groupId);
            bean.setCommand(CommonConstant.Command.REGISTER_SUCCESS);
            List<byte[]> bytes = BeanUtil.convertBeanToByte(metadata, null, bean);
            for (byte[] b:bytes){
                ByteBuffer buffer = ByteBuffer.wrap(b);
                channel.write(buffer);
                while (buffer.hasRemaining()){
                    channel.write(buffer);
                }
            }
        }
    }
}
