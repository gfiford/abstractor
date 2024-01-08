package com.fiford;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Optional;

public abstract class Abstractor<T> {
    
    @FunctionalInterface
    public static interface ActorRef<T> {
        void tell(T msg, ActorRef<T> sender);

        static <T> ActorRef<T> noSender() {
            return (msg, sender) -> {};
        }
    }

    @FunctionalInterface
    public static interface MessageReciever<MESSAGE> {
        void recieve(MESSAGE msg, ActorRef<MESSAGE> sender, MessageActor<MESSAGE> self);
    }

    public static class TypedActor<T> extends Abstractor<T> {

        public TypedActor(T instance) {
            super(instance);
        }

        public <R> Mailbox<R> tell(Function<T, R> action, final Mailbox<R> replyTo) {
            tell(i -> replyTo.post(action.apply(i)));
            return replyTo;
        }

        public <R> Mailbox<R> ask(Function<T, R> action) {
            return tell(action, new Mailbox<>());
        }

        void tell(Consumer<T> msg) {
            submit(() -> msg.accept(instance));
        }

    }

    public static class MessageActor<MESSAGE> extends Abstractor<MessageReciever<MESSAGE>>
            implements ActorRef<MESSAGE> {

        public MessageActor(MessageReciever<MESSAGE> instance) {
            super(instance);
        }

        public Mailbox<MESSAGE> ask(MESSAGE msg) {
            final Mailbox<MESSAGE> replyTo = new Mailbox<>();
            tell(msg, replyTo);
            return replyTo;
        }

        @Override
        public void tell(MESSAGE msg, ActorRef<MESSAGE> sender) {
            final MessageActor<MESSAGE> self = this;
            tell(ma -> ma.recieve(msg, sender, self));
        }

        void tell(Consumer<MessageReciever<MESSAGE>> msg) {
            submit(() -> msg.accept(instance));
        }
    }

    public static class Mailbox<T> implements ActorRef<T> {

        LinkedBlockingQueue<T> content = new LinkedBlockingQueue<>();

        public void post(T mesg) {
            content.add(mesg);
        }

        public Optional<T> recieve() {
            try {
                return Optional.of(content.take());
            } catch (InterruptedException e) {
                return Optional.empty();
            }
        }

        T poll(long timeout, TimeUnit unit) throws InterruptedException {
            return content.poll(timeout, unit);
        }

        @Override
        public void tell(T msg, ActorRef<T> sender) {
            post((T) msg);
        }
    }

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
            (r) -> new Thread(r));

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
        // add effectively is always true, only start a new task process if we not
        // already processing
        if (!alive.get()
                || mbox.queue.add(r) && mbox.queued.getAndIncrement() != 0)
            return;
        Abstractor.executor.submit(run());
    }

    Runnable run() {
        return () -> {
            int i = 0;
            while (i++ < 10 && alive.get() && mbox.run());
            if (!mbox.isEmpty()) Abstractor.executor.submit(run());
        };
    }

    public static void awaitComplete() {
        LockSupport.parkNanos(1000 * 1000);// anything just submitted time to get on the executor
        while (((ThreadPoolExecutor) executor).getPoolSize() != 0)
            LockSupport.parkNanos(1000 * 1000);
    }
}
