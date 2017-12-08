package com.liutaoyxz.yxzmq.broker;

public class ServerConfig {

    private int port = 11172;

    private String dataDir = "e://data2";

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
}
