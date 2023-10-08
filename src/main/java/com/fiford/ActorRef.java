package com.fiford;

public interface ActorRef<T> {
    void tell(T msg, ActorRef<T> sender);
    void tellException(Throwable e, ActorRef<T> sender);
}
