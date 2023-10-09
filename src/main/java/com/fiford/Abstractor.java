package com.fiford;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Abstractor<T>  {

    public static class ActorMailbox {
         final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
         final AtomicInteger queued = new AtomicInteger(0);
    }


    static ExecutorService executor = Executors.newCachedThreadPool(Thread.ofVirtual().factory());
    
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

    void submit(Runnable r) {
        //add effectively is always true, only start a new task process if we not already processing
        if (!alive.get() 
        || mbox.queue.add(r) && mbox.queued.getAndIncrement() != 0) return;
        Abstractor.executor.submit(run());
    }

    Runnable run() {
        return () -> {
            for(int i = 0; i < 10; i++) {
                Runnable run;
                if((run = mbox.queue.poll()) == null) return;
                run.run();
                mbox.queued.decrementAndGet();
            }
            if(!mbox.queue.isEmpty()) Abstractor.executor.submit(run());
        };
    }
}