package com.mathieuancelin.actors.cdi.api;

public interface ActorRef {

    String id();

    boolean buzy();

    void send(Object msg);

    void send(Object msg, String from);

    void send(Object msg, ActorURL from);

    ActorURL asLocalURL();
}
