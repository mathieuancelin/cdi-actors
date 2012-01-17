package com.mathieuancelin.actors.cdi.util;

import com.mathieuancelin.actors.cdi.ActorImpl;
import com.mathieuancelin.actors.cdi.api.ActorRef;
import java.util.List;

public class Broadcaster {

    private final List<ActorRef> actors;

    public Broadcaster(List<ActorRef> actors) {
        this.actors = actors;
    }

    public final void send(Object msg) {
        for (ActorRef actor : actors) {
            if (actor instanceof ActorImpl) {
                ((ActorImpl) actor).send(msg);
            } else {
                actor.send(msg);
            }
        }
    }

    public final void send(Object msg, ActorRef from) {
        for (ActorRef actor : actors) {
            if (actor instanceof ActorImpl) {
                ((ActorImpl) actor).send(msg, from);
            } else {
                actor.send(msg, from.id());
            }
        }
    }
}
