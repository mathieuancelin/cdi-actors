package com.mathieuancelin.actors.cdi.api;

public interface LocalActorRef extends ActorRef {

    void send(Object msg, ActorRef from);
}
