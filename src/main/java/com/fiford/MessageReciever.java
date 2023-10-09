package com.fiford;

public interface MessageReciever<MESSAGE>  {     
    void recieve(MESSAGE msg, ActorRef<MESSAGE> sender, ActorRef<MESSAGE> self);        
}