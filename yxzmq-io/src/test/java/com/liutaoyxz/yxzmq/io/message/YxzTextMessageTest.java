package com.liutaoyxz.yxzmq.io.message;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 14:03 2017/12/25
 * @Description:
 */
public class YxzTextMessageTest {
    @Test
    public void setText() throws Exception {
    }

    @Test
    public void getText() throws Exception {
        YxzTextMessage textMessage = new YxzTextMessage("text message");
        System.out.println(textMessage.getMessageClass());
        System.out.println(textMessage.getPropertyNames());
    }

}