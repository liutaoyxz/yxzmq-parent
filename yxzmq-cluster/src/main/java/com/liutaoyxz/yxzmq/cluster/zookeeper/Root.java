package com.liutaoyxz.yxzmq.cluster.zookeeper;

/**
 * @author Doug Tao
 * @Date: 15:36 2017/11/27
 * @Description: 根目录操作,/yxzmq
 */
public interface Root {


    /**
     * 检查根目录是否存在并且监视根目录
     * @return
     */
    boolean checkRoot();



}
