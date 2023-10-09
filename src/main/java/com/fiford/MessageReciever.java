package com.fiford;

public interface MessageReciever<MESSAGE>  {     
    void recieve(MESSAGE msg, ActorRef<MESSAGE> sender, MessageActor<MESSAGE> self);        
}