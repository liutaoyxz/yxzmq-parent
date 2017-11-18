package com.liutaoyxz.yxzmq.broker.channelhandler;

import com.liutaoyxz.yxzmq.broker.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Tao
 * @Date: 9:46 2017/11/15
 * @Description:
 */
public class DefaultChannelHandler extends AbstractChannelHandler {

    private DefaultChannelHandler(Logger log) {
        super(log);
    }

    public DefaultChannelHandler() {
        this(LoggerFactory.getLogger(DefaultChannelHandler.class));
    }

    @Override
    protected void afterConnected(Client client) {

    }

    @Override
    protected void afterDiscontected(Client client) {

    }
}
