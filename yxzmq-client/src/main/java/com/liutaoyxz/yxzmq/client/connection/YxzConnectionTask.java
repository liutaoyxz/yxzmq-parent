package com.liutaoyxz.yxzmq.broker.client.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * @author Doug Tao
 * @Date 下午1:26 2017/11/19
 * @Description:
 */
public class YxzConnectionTask implements Runnable {

    public static final Logger log = LoggerFactory.getLogger(YxzConnectionTask.class);

    public static final int SEND_DATA = 1;

    private YxzDefaultConnection connection;

    private int type;

    private List<byte[]> data;

    public List<byte[]> getData() {
        return data;
    }

    public void setData(List<byte[]> data) {
        this.data = data;
    }

    public YxzConnectionTask(YxzDefaultConnection connection, int type) {
        this.type = type;
        this.connection = connection;
    }


    @Override
    public void run() {
        if (this.type == SEND_DATA) {
            //发送数据
            YxzClientChannel cc = connection.applyChannel();
            SocketChannel sc = cc.getChannel();
            try {
                for (byte[] bytes : data) {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

                    sc.write(byteBuffer);
                    while (byteBuffer.hasRemaining()) {
                        sc.write(byteBuffer);
                    }
                }
            } catch (IOException e) {
                log.debug("send data error", e);
                e.printStackTrace();
            } finally {
                connection.returnChannel(cc);
            }
            return;
        }
    }


}
