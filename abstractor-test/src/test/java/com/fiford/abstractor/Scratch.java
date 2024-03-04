package com.fiford.abstractor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import com.fiford.Abstractor.ActorRef;
import com.fiford.Abstractor.Mailbox;
import com.fiford.Abstractor.MessageActor;
import com.fiford.Abstractor.TypedActor;
import com.fiford.Abstractor.MessageReciever;

public class Scratch {

    public static void main(String[] args) {
        messageActrs();
        typedActrs();
    }

    private static void typedActrs() {
        //wrap a hashSet in an actor
        TypedActor<Set<Integer>> intSet = new TypedActor<>(new HashSet<Integer>());

        //add some stuff in parallel
        IntStream.range(0, 10000)
            .parallel()//safe to do to a hash set in an actor
            .forEach(i -> intSet.apply(s -> s.add(i)));
        //show that we have all our stuff
        System.out.println(intSet.ask(s -> s.size()).recieve().get());
    }

    private static void messageActrs() {

        //functional interface based actors        
        MessageActor<Double> squareRouter = new MessageActor<>(
            (msg, sender, self) -> sender.tell(Math.sqrt(msg), self));

        System.out.println(squareRouter.ask(25.0).recieve().get());

        MessageActor<String> echoActr = new MessageActor<>(
            (msg, sender, self) -> sender.tell(msg, self));

        Mailbox<Boolean> resultMailbox = new Mailbox<>();
            //anonymous actors can have instance variables, defined withint their message recievers
        new MessageActor<>(new MessageReciever<String>() {
            int count = 0;
            @Override
            public void recieve(String msg, ActorRef<String> sender, MessageActor<String> self) {
                count++;
                if (count > 10) {
                    resultMailbox.post(true);
                    return;
                }
                System.out.println(count);
                sender.tell(""+count, self);
            }
        }).tell("hello", echoActr);
        
        //await for all of the messages 
        System.out.println(resultMailbox.recieve());
        
    }
    
}
