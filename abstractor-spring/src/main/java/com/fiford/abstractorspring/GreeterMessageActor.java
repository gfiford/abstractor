package com.fiford.abstractorspring;

import org.springframework.stereotype.Service;

import com.fiford.Abstractor.MessageActor;

@Service
public class GreeterMessageActor extends MessageActor<String> {
 

    public GreeterMessageActor() {
        super((msg, sender, self) -> {

            sender.tell("Hello "+ msg +"!", self);
        });
    }

}
