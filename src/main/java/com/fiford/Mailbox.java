package com.fiford;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

public class Mailbox<T> implements ActorRef<T> {

    LinkedBlockingQueue<T> content = new LinkedBlockingQueue<>(); 

    public void post(T mesg) {
        content.add(mesg);
    }

    public Optional<T> recieve() {
        try {
            return Optional.of(take());
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    T take() throws InterruptedException {
        return content.take();
    }

    @Override
    public void tell(T msg, ActorRef<T> sender) {
        post((T) msg);
    }
}
