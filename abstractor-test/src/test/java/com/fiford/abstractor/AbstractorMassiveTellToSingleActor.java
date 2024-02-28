package com.fiford.abstractor;

import java.util.BitSet;
import com.fiford.Abstractor.Mailbox;
import com.fiford.Abstractor.MessageActor;
import com.fiford.Abstractor.TypedActor;

public class AbstractorMassiveTellToSingleActor {
    
    public static void main(String[] args) {

        final int msgCount = 100000;
        final long start = System.currentTimeMillis();

        //mbox to tell when we are finished
        Mailbox<Integer> mbox = new Mailbox<>();
        //make our bit set wrapped in an actor - more fun
        TypedActor<BitSet> counter = new TypedActor<>(new BitSet());
        //set the bits high
        counter.apply(bs -> bs.set(0, msgCount));        
        
        MessageActor<Integer> reciever = new MessageActor<>((msg,sender,self) -> {
            counter.apply(bs -> bs.clear(msg));
            if (counter.ask(bs -> bs.isEmpty()).recieve().get())
                sender.tell(0, sender);
        });

        for(int i = 0; i < msgCount; i++)
            reciever.tell(i, mbox);

        System.out.println(mbox.recieve().get());
        System.out.println(System.currentTimeMillis() - start);
            

    }
}
