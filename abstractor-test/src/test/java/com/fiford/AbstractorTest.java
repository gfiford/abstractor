package com.fiford;

import org.junit.jupiter.api.Test;

import com.fiford.Abstractor.TypedActor;
import com.fiford.Abstractor.MessageActor;
import com.fiford.Abstractor.Mailbox;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AbstractorTest {

    @Test
    void testTypedActor() {
        TypedActor<String> actor = new TypedActor<>("Hello");

        Mailbox<String> mailbox = actor.ask(String::toUpperCase);
        assertEquals("HELLO", mailbox.recieve().orElse(null));
    }

    @Test
    void testMessageActor() {
    MessageActor<String> actor = new MessageActor<>((msg, sender, self) -> {
            sender.tell(msg.toUpperCase(), self);
        });

        Mailbox<String> mailbox = actor.ask("Hello");
        assertEquals("HELLO", mailbox.recieve().orElse(null));
    }

    @Test
    void testMailbox() throws InterruptedException {
        Mailbox<String> mailbox = new Mailbox<>();
        mailbox.post("Hello");
        mailbox.post("World");

        assertEquals("Hello", mailbox.recieve().orElse(null));
        assertEquals("World", mailbox.poll(100, TimeUnit.MILLISECONDS));
        assertTrue(mailbox.isEmpty());
    }

    @Test
    void testActorLifecycle() {
        Abstractor<String> actor = new TypedActor<>("Hello");
        assertTrue(actor.alive.get());
        assertTrue(actor.kill());
        assertFalse(actor.alive.get());
    }

    @Test
    void testAbstractorApply() {
        TypedActor<StringBuilder> actor = new TypedActor<>(new StringBuilder());

        actor.apply(sb -> sb.append("Hello"));
        actor.apply(sb -> sb.append(" World"));

        Mailbox<String> mailbox = actor.ask(StringBuilder::toString);
        assertEquals("Hello World", mailbox.recieve().orElse(null));
    }
}