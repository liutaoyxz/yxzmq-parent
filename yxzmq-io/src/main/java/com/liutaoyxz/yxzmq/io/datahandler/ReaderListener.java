package com.liutaoyxz.yxzmq.io.datahandler;

/**
 * @author Doug Tao
 * @Date: 14:05 2017/11/15
 * @Description: 读取数据的监听器,当读取到完整的数据是,需要触发 readComplete 方法
 */
public interface ReaderListener {


    void readComplete();

}
