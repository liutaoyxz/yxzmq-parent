package com.liutaoyxz.yxzmq.broker.datahandler;

import com.liutaoyxz.yxzmq.broker.Client;
import com.liutaoyxz.yxzmq.broker.channelhandler.ChannelHandler;
import com.liutaoyxz.yxzmq.broker.datahandler.analyser.DefaultDataAnalyner;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Doug Tao
 * @Date: 16:09 2017/11/15
 * @Description:
 */
public abstract class AbstractChannelReader implements ChannelReader {

    private Logger log;

    private ChannelHandler handler;

    private AtomicInteger readCount = new AtomicInteger(1);

    protected AbstractChannelReader(Logger log,ChannelHandler handler) {
        this.handler = handler;
        this.log = log;
    }

    /**
     *  TODO: 2017/11/15 读的线程,先和每个channel对应生成一个线程,实现协议后优化
     *  采用了cache的方式,实际使用中 最大的线程量不会超过socketChannel的数量,也就是@{@link Client}的数量
     */
    private ExecutorService executorService = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 5L,
            TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("read-thread"+readCount.getAndIncrement());
            return thread;
        }
    });

    /**
     * channel 的读取方式是  每一个Client中都包含一个ReaderContainer,并且唯一
     * channel 的读取过程必须是顺序执行的,也就是要保证单线程,这里采用加锁的方式
     * @see Client
     * @param client
     */
    @Override
    public void startRead(final Client client) {
        if (client.startRead()){
            executorService.execute(client.getDataReadTask());
        }
    }

}
