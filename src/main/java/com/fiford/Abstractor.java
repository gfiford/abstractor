package com.fiford;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Abstractor<T, MSG> {
    Mailbox<MSG> mbox = new Mailbox<>();
    final Thread stage;
    T instance;

    Abstractor(T instance) {
        this.instance = instance;
        stage = Thread.ofVirtual().factory().newThread(getRuntime());
        stage.start();
    }

    Mailbox<MSG> getMailbox() {
        return mbox;
    }

    abstract void tell(MSG msg);

    abstract Runnable getRuntime();

    T getInstance() {
        return instance;
    }

    boolean alive() {
        return stage.isAlive();
    }

    boolean kill() {
        stage.interrupt();
        return !alive();
    }

    public record Msg<REQ, REP>(REQ req, REP rep) {
    }

    public static class MessageActor<REQUEST, REPLY>
            extends Abstractor<BiConsumer<REQUEST, Mailbox<REPLY>>, Msg<REQUEST, Mailbox<REPLY>>> {

        Runnable getRuntime() {
            return () -> {
                while (true) {
                    Msg<REQUEST, Mailbox<REPLY>> msg = null;
                    try {
                        msg = getMailbox().take();
                        instance.accept(msg.req(), msg.rep());
                    } catch (InterruptedException e) {
                        return;
                    } catch (Throwable t) {
                        if (msg != null)
                            msg.rep().addException(t);
                    }
                }
            };
        }

        public MessageActor(BiConsumer<REQUEST, Mailbox<REPLY>> instance) {
            super(instance);
        }

        Mailbox<REPLY> ask(REQUEST msg) {
            Mailbox<REPLY> replyTo = new Mailbox<>();
            mbox.add(new Msg<REQUEST, Mailbox<REPLY>>(msg, replyTo));
            return replyTo;
        }

        @Override
        void tell(Msg<REQUEST, Mailbox<REPLY>> msg) {
            mbox.add(msg);
        }
    }

    public static class Mailbox<T> {

        final AtomicReference<Throwable> error = new AtomicReference<>();
        final LinkedBlockingQueue<T> content = new LinkedBlockingQueue<>();

        void add(T e) {
            content.add(e);
        }

        void addException(Throwable t) {
            error.set(t);
            add(null);
        }

        public Optional<T> recieve() {
            try {
                return Optional.of(content.take());
            } catch (InterruptedException e) {
                if (error.get() == null)
                    error.set(e);
                return Optional.empty();
            }
        }

        T take() throws InterruptedException {
            return content.take();
        }
    }

    public static class TypedActor<T> extends Abstractor<T, Consumer<T>> {

        public TypedActor(T instance) {
            super(instance);
        }

        public void tell(Consumer<T> action) {
            mbox.add(action);
        }

        public <R> Mailbox<R>  tell(Function<T, R> action, Mailbox<R>  replyTo) {
            mbox.add(t -> {
                try {
                    replyTo.add(action.apply(t));
                } catch (Throwable e) {
                    replyTo.addException(e);
                }
            });
            return replyTo;
        }

        public <R> Mailbox<R> tell(Function<T, R> action) {
            return tell(action, new Mailbox<>());
        }

        @Override
        Runnable getRuntime() {
            return () -> {
                while (true) {
                    try {
                        getMailbox().take().accept(getInstance());

                    } catch (InterruptedException e) {
                        return;
                    } catch (Throwable t) {

                    }
                }
            };
        }
    }
}