package com.liutaoyxz.yxzmq.broker.messagehandler;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.client.ServerClient;
import com.liutaoyxz.yxzmq.broker.client.ServerClientManager;
import com.liutaoyxz.yxzmq.broker.storage.MessageContainer;
import com.liutaoyxz.yxzmq.common.util.ProtostuffUtil;
import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;
import com.liutaoyxz.yxzmq.io.wrap.QueueMessage;
import com.liutaoyxz.yxzmq.io.wrap.TopicMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Doug Tao
 * @Date: 9:56 2017/12/8
 * @Description: 处理消息
 */
public class NettyMessageHandler {

    public static final Logger log = LoggerFactory.getLogger(NettyMessageHandler.class);


    public static void handlerProtocolBean(ProtocolBean bean, ServerClient client) throws IOException {
        if (!checkBean(bean)){
            return;
        }
        final int command = bean.getCommand();
        final MessageDesc desc = getDesc(bean);
        final String msg = getMessage(bean);
        String zkName = bean.getZkName();
        String id = client.id();
        switch (command){
            case CommonConstant.Command.SEND:
                //发送过来的消息
                handlerMessage(desc,msg);
                break;


            case CommonConstant.Command.BROKER_SUBJECT_REGISTER:
                //主体注册
                ServerClientManager.subjectChange(id,zkName);
                break;
            case CommonConstant.Command.CLIENT_REGISTER:
                //客户端注册
                ServerClientManager.clientRegister(id,zkName);

            default:
                log.debug("handler protocolBean error,command is {}",command);
                break;
        }


    }

    private static boolean checkBean(ProtocolBean bean){
        if (bean == null){
            return false;
        }
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
    private static void handlerMessage(MessageDesc desc, String msg) throws IOException {
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
}
