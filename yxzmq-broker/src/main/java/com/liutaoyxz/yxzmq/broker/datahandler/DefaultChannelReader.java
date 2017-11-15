package com.liutaoyxz.yxzmq.broker.datahandler;

import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Tao
 * @Date: 15:55 2017/11/15
 * @Description:
 */
public class DefaultChannelReader extends AbstractChannelReader {

    private DefaultChannelReader(Logger log, ChannelHandler handler) {
        super(log,handler);
    }

    public DefaultChannelReader(ChannelHandler handler){
        this(LoggerFactory.getLogger(DefaultChannelReader.class),handler);
    }
}
