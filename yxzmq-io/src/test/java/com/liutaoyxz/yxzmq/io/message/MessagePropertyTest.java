package com.liutaoyxz.yxzmq.io.message;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Doug Tao
 * @Date: 10:31 2017/12/22
 * @Description:
 */
public class MessagePropertyTest {
    @Test
    public void getType() throws Exception {
        Object o = new Integer(10);
        System.out.println(o.getClass() == Integer.class);
    }

    @Test
    public void msgTest() throws Exception{
        YxzTextMessage text = new YxzTextMessage("fuck");
        text.setBooleanProperty("boolean",false);
        System.out.println(text.getBooleanProperty("b"));

    }

}