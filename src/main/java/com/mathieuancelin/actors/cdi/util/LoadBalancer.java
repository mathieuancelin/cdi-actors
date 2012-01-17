package com.mathieuancelin.actors.cdi.util;

import com.mathieuancelin.actors.cdi.ActorImpl;
import com.mathieuancelin.actors.cdi.api.ActorRef;
import com.mathieuancelin.actors.cdi.api.Messages.Broadcast;
import java.util.Iterator;
import java.util.List;

public class LoadBalancer {

    private final List<ActorRef> actors;
    
    private Iterator<ActorRef> it;

    public LoadBalancer(List<ActorRef> actors) {
        this.actors = actors;
        this.it = actors.iterator();
    }

    public final void send(Broadcast msg) {
        broadcast(msg.message(), msg.from().getOrNull());
    }

    public final void send(Object msg) {
        chooseAndSend(msg, null);
    }

    public final void send(Object msg, ActorRef from) {
        chooseAndSend(msg, from);
    }

    private void chooseAndSend(Object msg, ActorRef from) {
        if (!it.hasNext()) {
            it = actors.iterator();
        }
        ActorRef a = it.next();
        if (!a.buzy()) {
            a.send(msg, from.id());
        } else {
            boolean sent = false;
            for (ActorRef bis : actors) {
                if (!bis.buzy()) {
                    a.send(msg, from.id());
                    sent = true;
                }
            }
            if (!sent) {
                a.send(msg, from.id());
            }
        }
    }

    private void broadcast(Object message, ActorRef from) {
        for (ActorRef actor : actors) {
            if (actor instanceof ActorImpl) {
                ((ActorImpl) actor).send(message, from);
            } else {
                actor.send(message, from.id());
            }
        }
    }
}
