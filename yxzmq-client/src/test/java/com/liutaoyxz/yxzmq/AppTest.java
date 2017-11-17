package com.liutaoyxz.yxzmq;

import com.liutaoyxz.yxzmq.common.util.ProtostuffUtil;
import com.liutaoyxz.yxzmq.io.protocol.Metadata;

import java.util.Arrays;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static void main(String[] args) {
        ProtocolBeanTest test = new ProtocolBeanTest();
        test.setCommand(1);
        test.setTitle("title");
        Metadata metadata = new Metadata();
        metadata.setClientId("xxx1");

        byte[] bytes = ProtostuffUtil.serializable(test);

        System.out.println(Arrays.toString(bytes));


        ProtocolBeanTest obj = ProtostuffUtil.get(bytes, ProtocolBeanTest.class);
        System.out.println(obj);


    }

}
