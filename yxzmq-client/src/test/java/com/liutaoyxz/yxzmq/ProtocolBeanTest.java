package com.liutaoyxz.yxzmq;

import com.liutaoyxz.yxzmq.io.protocol.Metadata;

import java.util.Arrays;

/**
 * @author Doug Tao
 */
public class ProtocolBeanTest {

    public ProtocolBeanTest() {
    }

    /**
     * 命令
     */
    private int command;

    /**
     * 主题
     */
    private String title;


    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "ProtocolBeanTest{" +
                "command=" + command +
                ", title='" + title + '\'' +
                '}';
    }
}
