package com.mathieuancelin.actors.cdi;

import akka.actor.UntypedActor;

public class Pong extends UntypedActor {
       
    public void onReceive(Object o) {
        if (o instanceof String) {
            if (((String) o).startsWith("ping")) {
                ActorsTest.down.countDown();
                sender().tell("pong", self());
                System.out.println("PONG");
            } else if (((String) o).startsWith("stop")) {
                System.out.println("STOP PONG");    
                getContext().stop(self());
            }
        } else {
            System.err.println("[PONG] Unknow message : " + o.toString());
        }
    }
}
