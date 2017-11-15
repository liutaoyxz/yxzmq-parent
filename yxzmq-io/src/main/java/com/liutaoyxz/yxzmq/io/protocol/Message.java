package com.liutaoyxz.yxzmq.io.protocol;

/**
 * @author Doug Tao
 * @Date: 14:13 2017/11/15
 * @Description:
 */
public interface Message<T> {

    /**
     * 返回指定类型的消息内容
     * @return
     */
    T getContent();

}
