package com.liutaoyxz.yxzmq.io.protocol;

/**
 * Created by liutao on 2017/11/14.
 */
public class TestCommand implements Command {

    public boolean opeartor(String cliendId) {
        System.out.println("testCommand opeartor");
        return true;
    }
}
