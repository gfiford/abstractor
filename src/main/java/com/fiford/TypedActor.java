package com.fiford;

import java.util.function.Consumer;
import java.util.function.Function;

public class TypedActor<T> extends Abstractor<T, Consumer<T>> {

    public TypedActor(T instance) {
        super(instance);
    }

    public <R> Mailbox<R>  tell(Function<T, R> action, final Mailbox<R>  replyTo) {
        tell(i -> {
            try {
                replyTo.post(action.apply(i));
            } catch (Throwable t) {
                replyTo.postException(t);
                throw t;
            }

        });
        return replyTo;
    }

    public <R> Mailbox<R> ask(Function<T, R> action) {
        return tell(action, new Mailbox<>());
    }
    
    @Override
    void tell(Consumer<T> msg) {
        submit(() -> msg.accept(instance));
    }

}