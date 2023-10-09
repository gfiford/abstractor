package com.fiford;

import java.util.function.Consumer;
import java.util.function.Function;

public class TypedActor<T> extends Abstractor<T> {

    public TypedActor(T instance) {
        super(instance);
    }

    public <R> Mailbox<R>  tell(Function<T, R> action, final Mailbox<R>  replyTo) {
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