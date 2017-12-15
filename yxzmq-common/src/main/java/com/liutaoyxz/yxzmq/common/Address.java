package com.liutaoyxz.yxzmq.common;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Doug Tao
 * @Date: 10:00 2017/12/15
 * @Description:
 */
public class Address {

    private String ip;

    private int port;

    private String order;

    private Address(String ip,int port,String order){
        this.ip = ip;
        this.port = port;
        this.order = order;
    }

    public static Address createAddress(String zkName){
        String[] strings = StringUtils.split(zkName, "-");
        String[] ss = StringUtils.split(strings[0], ":");
        String ip = ss[0];
        int port = Integer.valueOf(ss[1]);
        return new Address(ip,port,strings[1]);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "Address{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
