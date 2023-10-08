package com.fiford;

import java.util.function.Consumer;

import com.fiford.Abstractor.MessageActorClass;

public class MessageActor<MESSAGE> extends Abstractor<MessageActorClass<MESSAGE>,Consumer<MessageActorClass<MESSAGE>>> implements ActorRef<MESSAGE> {


    public MessageActor(MessageActorClass<MESSAGE> instance) {
        super(instance);
        instance.ref = this;
    }

    public Mailbox<MESSAGE> ask(MESSAGE msg) {
        final Mailbox<MESSAGE> replyTo = new Mailbox<>();
        tell(msg, replyTo);
        return replyTo;
    }

 

    @Override
    public void tell(MESSAGE msg, ActorRef<MESSAGE> sender) {
        tell(ma -> {
            ma.recieve(msg, sender);
        });
    }

    @Override
    public void tellException(Throwable e, ActorRef<MESSAGE> sender) {
        tell(ma -> {
            ma.recieve(e, sender);
        });
    }

    @Override
    void tell(Consumer<MessageActorClass<MESSAGE>> msg) {
        submit(() -> msg.accept(instance));
    }



}