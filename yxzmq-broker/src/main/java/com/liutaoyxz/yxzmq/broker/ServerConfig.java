package com.liutaoyxz.yxzmq.broker;

/**
 * 服务端配置
 * @author DougTao
 */
public class ServerConfig {

    private int port = 11172;

    private String dataDir = "/Users/liutao/yxzmqData2";

    private String zkConnectStr = "127.0.0.1:2181";

    public String getDataDir(){
        return this.dataDir;
    }

    public void setDataDir(String dataDir){
        this.dataDir = dataDir;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getZkConnectStr() {
        return zkConnectStr;
    }

    public void setZkConnectStr(String zkConnectStr) {
        this.zkConnectStr = zkConnectStr;
    }
}
