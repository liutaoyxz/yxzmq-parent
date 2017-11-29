/**
 * @author Doug Tao
 * @Date 下午6:10 2017/11/18
 * @Description:
 *
 *
 *  /yxzmq
 *      /brokers -s
 *          /127.0.0.1:11171 -e
 *          /127.0.0.1:11172 -e
 *      /clients -s
 *          /127.0.0.1 -e
 *          /127.0.0.1 -e
 *      /topics -s
 *          /t1 -s
 *              /client1 -e
 *              /client2 -e
 *          /t2 -s
 *      /queues
 *          /q1 -s
 *              /client1 -e
 *              /client2 -e
 *          /q2 -s
 *
 *
 */
package com.liutaoyxz.yxzmq.cluster.zookeeper;