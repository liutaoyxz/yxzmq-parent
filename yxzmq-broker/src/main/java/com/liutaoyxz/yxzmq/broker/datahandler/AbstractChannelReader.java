package com.liutaoyxz.yxzmq.broker.datahandler;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.datahandler.analyser.DefaultDataAnalyner;
import com.sun.xml.internal.bind.v2.TODO;
import org.slf4j.Logger;

import java.util.concurrent.*;

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

    /**
     *  TODO: 2017/11/15 读的线程,先和每个channel对应生成一个线程,实现协议后优化
     */
    private ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 5L,
            TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("read-thread");
            return thread;
        }
    });

    @Override
    public void startRead(final Client client) {
        if (!client.startRead()){
            return ;
        }
        executorService.execute(getTask(client));
    }

    /**
     * 获取客户端的task 对象
     * @param client
     * @return
     * TODO 现在每个客户端的数据处理采用的是单线程,所以目前不存在并发操作
     */
    private Runnable getTask(Client client){
        String id = client.id();
        Runnable task = idToTask.get(id);
        if(idToTask.get(id) == null){
            task = new DefaultDataAnalyner(client,handler);
            idToTask.putIfAbsent(id,task);
            task = idToTask.get(id);
        }
        return task;
    }

}
