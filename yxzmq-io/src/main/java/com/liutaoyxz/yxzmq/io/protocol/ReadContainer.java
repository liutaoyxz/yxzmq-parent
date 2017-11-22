package com.liutaoyxz.yxzmq.io.protocol;

import com.liutaoyxz.yxzmq.common.util.ProtostuffUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author Doug Tao
 * @Date: 8:34 2017/11/16
 * @Description: 读取byte 数据,转换成ProtocolBean, 目前不支持多线程操作
 */
public class ReadContainer {

    private Logger log = LoggerFactory.getLogger(ReadContainer.class);

    public static final String DEFAULT_CHARSET = "utf-8";

    public static final int METADATA_SIZE_LENGTH = 10;

    private Metadata metadata;

    private byte[] metadataLenghtBytes = new byte[METADATA_SIZE_LENGTH];

    private byte[] metadataBytes ;

    private byte[] beanBytes ;

    private int metadataReadPosition = 0;

    private int metadataBytesReadPosition = 0;

    private int beanBytesReadPosition = 0;

    private int metadataBytesLenght;

    private LinkedBlockingQueue<ProtocolBean> beanQueue = new LinkedBlockingQueue<>();

    private volatile int stage = Stage.READ_LENGHT;

    public boolean haveBean(){

        return false;
    }

    public ProtocolBean getBean(){

        return null;
    }

    public void read(ByteBuffer buffer){
        int position = buffer.position();
        int limit = buffer.limit();
        while (position < limit){
            switch (this.stage){

                case Stage.READ_LENGHT:
                    //读取metadata 长度
                    if (metadataReadPosition < METADATA_SIZE_LENGTH){
                        //没有读满
                        metadataLenghtBytes[metadataReadPosition++] = buffer.get(position++);
                        break;
                    }
                    //已经读满,等待生成metadata 实例
                    metadataBytesLenght = Integer.valueOf(new String(metadataLenghtBytes, Charset.forName(DEFAULT_CHARSET)));
                    metadataBytes = new byte[metadataBytesLenght];
                    this.stage = Stage.READ_METADATA;
                    metadataReadPosition = 0;
                    break;
                case Stage.READ_METADATA:
                    //正在读取metadata 的序列化字节
                    if (metadataBytesReadPosition < metadataBytesLenght){
                        metadataBytes[metadataBytesReadPosition++] = buffer.get(position++);
                        break;
                    }
                    // 已经读满
                    metadata = ProtostuffUtil.get(metadataBytes,Metadata.class);
                    this.stage = Stage.READ_BEAN;
                    metadataBytesReadPosition = 0;
                    beanBytes = new byte[metadata.getBeanSize()];
                    break;
                case Stage.READ_BEAN:
                    //正在读取ProtocolBean 的序列化字节
                    if (beanBytesReadPosition < metadata.getBeanSize()){
                        //没有读满
                        beanBytes[beanBytesReadPosition++] = buffer.get(position++);
                        if (beanBytesReadPosition == metadata.getBeanSize()){
                            ProtocolBean bean = ProtostuffUtil.get(beanBytes,ProtocolBean.class);
                            log.debug("read a protocolBean {}",bean);
                            beanQueue.add(bean);
                            beanBytesReadPosition = 0;
                            this.stage = Stage.READ_LENGHT;
                        }
                        break;
                    }
                default:
                    log.error("read error,stage is {}",stage);
                    break;

            }
        }
//        buffer.
    }


    public List<ProtocolBean> flush(){
        List<ProtocolBean> result = new ArrayList<>();
        ProtocolBean bean = beanQueue.poll();
        while (bean != null){
            result.add(bean);
            bean = beanQueue.poll();
        }
        return result;
    }


    public static void main(String[] args) {
        ReadContainer c = new ReadContainer();
//        c.beanQueue.take();
    }


    static class Stage{

        static final int READ_LENGHT = 1;
        static final int READ_METADATA = 1 << 2;
        static final int READ_BEAN = 1 << 4;

    }

}
