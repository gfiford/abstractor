package com.fiford.abstractor;

import com.fiford.Abstractor.ActorRef;
import com.fiford.Abstractor.Mailbox;
import com.fiford.Abstractor.MessageActor;
import com.fiford.Abstractor.MessageReciever;
import com.fiford.Abstractor.TypedActor;

public class AbstractorSharedState {
    
	public static final int ACTORS = 10000000;


    public static void main(String[] args) {
        messageBased();
        typedBased();

    }

    public static class Counter {
        int count;
        private Mailbox<Boolean> fullLatch;

        Counter(Mailbox<Boolean> fullLatch) {
            this.fullLatch = fullLatch;
        }

        void inc() {
            if(++count == ACTORS)
                fullLatch.post(true);
        }
    }
    private static void typedBased() {
        long start = System.currentTimeMillis();
        final Mailbox<Boolean> mbox = new Mailbox<>();
        TypedActor<Counter> runner = new TypedActor<>(new Counter(mbox));
        for(int i = 0; i < ACTORS; i++) runner.apply(r -> r.inc());
            
        Boolean result = mbox.recieve().get();
        System.out.println(result.toString() +" typed in  "+ (System.currentTimeMillis() - start));
    }

    private static void messageBased() {
        long start = System.currentTimeMillis();
        final Mailbox<Boolean> mbox = new Mailbox<>();
        MessageActor<Integer> runner = new MessageActor<>(new MessageReciever<Integer>() {
            int count = 0;
            @Override
            public void recieve(Integer msg, ActorRef<Integer> sender, MessageActor<Integer> self) {
                count++;
                if (count == ACTORS) mbox.post(true);
            }
        });
        for (int i = 0; i < ACTORS; i++) {
            runner.tell(i, runner);
        }
        Boolean result = mbox.recieve().get();
        System.out.println(result.toString() +" message in  "+ (System.currentTimeMillis() - start));
    }

    
}
