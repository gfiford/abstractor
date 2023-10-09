package com.fiford;

import java.util.function.Consumer;

public class MessageActor<MESSAGE> extends Abstractor<MessageReciever<MESSAGE>> implements ActorRef<MESSAGE> {

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