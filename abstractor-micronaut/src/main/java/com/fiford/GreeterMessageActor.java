package com.fiford;

import com.fiford.Abstractor.MessageActor;

import jakarta.inject.Singleton;

@Singleton
public class GreeterMessageActor extends MessageActor<String> {
 

    public GreeterMessageActor() {
        super((msg, sender, self) -> {

            sender.tell("Hello "+ msg, self);
        });
    }

}
