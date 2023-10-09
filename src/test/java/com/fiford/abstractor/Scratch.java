package com.fiford.abstractor;

import com.fiford.Abstractor;
import com.fiford.ActorRef;
import com.fiford.MessageActor;
import com.fiford.MessageReciever;

public class Scratch {

    public static void main(String[] args) {
        //functional actors
        MessageActor<String> printCheckerActor = new MessageActor<>((msg, sender, self) -> {
                System.out.println(msg);
                sender.tell("Check", self);
            });

        //anonymous actors can have instance variables
        MessageReciever<String> reciever = new MessageReciever<String>() {

            int count = 0;
            @Override
            public void recieve(String msg, ActorRef<String> sender, MessageActor<String> self) {
                System.out.println(msg);
                if (count > 1000) return;
                sender.tell(""+count++, self);
            }
            
        };

        printCheckerActor.tell("", new MessageActor<>(reciever));
        Abstractor.awaitComplete();

        MessageReciever<Integer> sqrt = (msg, sender, self) -> {
            Double sq = Math.sqrt(msg);
            sender.tell((int)Math.round(sq), sender);
        };
        

    }
    
}
