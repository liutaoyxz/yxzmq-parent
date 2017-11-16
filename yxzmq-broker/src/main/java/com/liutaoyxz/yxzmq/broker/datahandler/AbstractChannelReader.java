package com.liutaoyxz.yxzmq.broker.datahandler;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.datahandler.analyser.DefaultDataAnalyner;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Doug Tao
 * @Date: 16:09 2017/11/15
 * @Description:
 */
public abstract class AbstractChannelReader implements ChannelReader {

    private Logger log;

    private ChannelHandler handler;

    private final ConcurrentHashMap<String,Runnable> idToTask = new ConcurrentHashMap<>();

    protected AbstractChannelReader(Logger log,ChannelHandler handler) {
        this.handler = handler;
        this.log = log;
    }

    // TODO: 2017/11/15 读的线程,先和每个channel对应生成一个线程,实现协议后优化
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void startRead(final Client client) {
        if (!client.startRead()){
            return ;
        }
        executorService.execute(getTask(client));
    }


    private Runnable getTask(Client client){
        String id = client.id();
        Runnable task = idToTask.get(id);
        if(idToTask.get(id) == null){
            task = new DefaultDataAnalyner(client,handler);
            idToTask.putIfAbsent(id,task);
        }
        return task;
    }

}
