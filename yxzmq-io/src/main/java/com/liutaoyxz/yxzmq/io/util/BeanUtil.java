package com.liutaoyxz.yxzmq.io.util;

import com.liutaoyxz.yxzmq.io.protocol.MessageDesc;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;
import com.liutaoyxz.yxzmq.io.protocol.ProtocolBean;
import com.liutaoyxz.yxzmq.io.protocol.ReadContainer;
import com.liutaoyxz.yxzmq.io.protocol.constant.CommonConstant;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Doug Tao
 * @Date: 10:39 2017/11/21
 * @Description:
 */
public class BeanUtil {

    public static List<byte[]> convertBeanToByte(Metadata metadata,MessageDesc desc ,ProtocolBean bean){
        List<byte[]> result = new ArrayList<>();
        //发布消息
        if (desc != null){
            byte[] descBytes = ProtostuffUtil.serializable(desc);
            bean.setDescBytes(descBytes);
            bean.setDescClass(desc.getClass().getName());
        }
        byte[] beanBytes = ProtostuffUtil.serializable(bean);
        metadata.setBeanSize(beanBytes.length);
        byte[] metadataBytes = ProtostuffUtil.serializable(metadata);
        int metadataLenght = metadataBytes.length;
        byte[] lenght = ProtostuffUtil.fillMetadataLength(metadataLenght).getBytes(Charset.forName(ReadContainer.DEFAULT_CHARSET));
        result.add(lenght);
        result.add(metadataBytes);
        result.add(beanBytes);
        return result;
    }

}
