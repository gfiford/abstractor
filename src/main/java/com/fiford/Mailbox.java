package com.fiford;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.fiford.Mailbox.ImmutableQueue.QueueNode;

public class Mailbox<T> implements ActorRef<T> {

    public static record ImmutableQueue<T>(QueueNode<T> head, QueueNode<T> tail) {

        public static class QueueNode<T> {
            T item;
            QueueNode<T> next;

            QueueNode(T item, QueueNode<T> next) {
                this.item = item;
                this.next = next;
            }
        }

        boolean empty() {
            return head == null;
        }

        ImmutableQueue<T> append(T e) {
            if (this.head == null) {
                return new ImmutableQueue<>(new QueueNode<>(e, null), null);
            }
            if (this.tail == null) {
                QueueNode<T> tail = new QueueNode<>(e, null);
                return new ImmutableQueue<>(new QueueNode<>(this.head.item, tail), tail);
            }
            QueueNode<T> newtail = new QueueNode<>(e, null);
            this.tail.next = newtail;
            return new ImmutableQueue<>(this.head, newtail);
        }

        ImmutableQueue<T> decap() {
            if (this.head == null || this.head.next == null) {
                return new ImmutableQueue<>(null, null);
            }
            return (head.next == null) ? new ImmutableQueue<>(head.next, null) : new ImmutableQueue<>(head.next, tail);
        }
    }

    private final ReentrantLock takelock = new ReentrantLock();
    private final ReentrantLock putlock = new ReentrantLock();

    private final Condition signal = putlock.newCondition();

    final AtomicReference<Throwable> error = new AtomicReference<>();
    final AtomicReference<QueueNode<T>> head = new AtomicReference<>();
    final AtomicReference<QueueNode<T>> tail = new AtomicReference<>();

    LinkedBlockingQueue<T> content = new LinkedBlockingQueue<>(); 

    public void post(T mesg) {
        content.add(mesg);
        
        // putlock.lock();
        // try {
        //     tail.getAndUpdate(o -> {
        //         QueueNode<T> newTail = new QueueNode<>(mesg, null);
        //         if (o != null) o.next = newTail;
        //         return newTail;
        //     });
        //     head.getAndUpdate(h -> h == null ? tail.get() : h);
        // } finally {
        //     putlock.unlock();
        // }
            
        // takelock.lock();
        // try {
        //     signal.signal();
        // } finally {
        //     takelock.unlock();
        // }
    }

    public void postException(Throwable t) {
        error.set(t);
        post(null);
    }

    public Optional<T> recieve() {
        try {
            return Optional.of(take());
        } catch (InterruptedException e) {
            if (error.get() == null)
                error.set(e);
            return Optional.empty();
        }
    }

    T take() throws InterruptedException {
        return content.take();
        // takelock.lockInterruptibly();
        // try {
        //     while (head.get() == null) {
        //         signal.awaitUninterruptibly();
        //     }
        //     return head.getAndUpdate(o -> o.next).item;
        // } finally {
        //     takelock.unlock();
        // }
    }

    @Override
    public void tell(T msg, ActorRef<T> sender) {
        post((T) msg);
    }

    @Override
    public void tellException(Throwable e, ActorRef<T> sender) {
        postException(e);
    }
}
