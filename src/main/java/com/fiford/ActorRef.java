package com.fiford;

@FunctionalInterface
public interface ActorRef<T> {
    void tell(T msg, ActorRef<T> sender);

    static <T> ActorRef<T> noSender() {
        return (msg, sender) -> {};
    }
}
