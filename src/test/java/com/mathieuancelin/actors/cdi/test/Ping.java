package com.mathieuancelin.actors.cdi.test;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class Ping extends UntypedActor {
        
    private ActorRef pong;
    
    @Override
    public void preStart() {
        this.pong = getContext().actorFor("../pong");
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof String) {
            if (((String) o).startsWith("pong")) {
                System.out.println("PING");
                if (ActorsTest.down.getCount() == 0) {
                    pong.tell("stop", getSelf());
                    getContext().stop(getSelf());
                    System.out.println("STOP PING");
                } else {
                    pong.tell("ping", getSelf());
                }
            } else if (((String) o).startsWith("start")) {
                pong.tell("ping", getSelf());
                System.out.println("START PING");
            }
        } else {
            System.err.println("[PING] Unknow message : " + o.toString());
        }
    }
}
