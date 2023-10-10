package com.fiford;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public abstract class Abstractor<T>  {

    public static class ActorMailbox {
         final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
         final AtomicInteger queued = new AtomicInteger(0);
         
         boolean isEmpty() {
            return queued.get() == 0;
         }

         boolean run() {
            if (isEmpty()) return false;
            Runnable task = queue.poll();
            if (task == null) return false;
            try {
                task.run();
            } finally {
                queued.decrementAndGet();    
            }
            return true;
         }


    }


    static ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      10L, TimeUnit.MILLISECONDS,
                                      new SynchronousQueue<>(),
                                      Thread.ofVirtual().factory());
    
    T instance;
    ActorMailbox mbox = new ActorMailbox();
    AtomicBoolean alive = new AtomicBoolean(true);

    Abstractor(T instance) {
        this.instance = instance;
    }

    T getInstance() {
        return instance;
    }

    boolean kill() {
        return alive.getAndSet(false);
    }

    public void apply(Consumer<T> consumer) {
        submit(() -> consumer.accept(instance));
    }

    public void submit(Runnable r) {
        //add effectively is always true, only start a new task process if we not already processing
        if (!alive.get() 
        || mbox.queue.add(r) && mbox.queued.getAndIncrement() != 0) return;
        Abstractor.executor.submit(run());
    }

    Runnable run() {
        return () -> {
            int i = 0;
            while (i++ < 10 && alive.get() && mbox.run());
            if(!mbox.isEmpty()) Abstractor.executor.submit(run());
        };
    }

    public static void awaitComplete() {
        LockSupport.parkNanos(1000*1000);//anything just submitted time to get on the executor
        while(((ThreadPoolExecutor) executor).getPoolSize() != 0) LockSupport.parkNanos(1000*1000);        
    }
}