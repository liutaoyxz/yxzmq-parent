/**
 * @author Doug Tao
 * @Date 下午6:10 2017/11/18
 * @Description:
 *
 * 每一个broker都会找另一个broker 作为自己的镜像,当镜像发生故障时,cluster会通知本体的broker和故障broker的镜像broker;
 * 故障broker的镜像会立即读取文件中的数据加载到自己的内存中作为主数据;
 * cluster整体刷新镜像规则,保证每一台broker又会拥有自己的镜像;
 *
 *
 */
package com.liutaoyxz.yxzmq.cluster;