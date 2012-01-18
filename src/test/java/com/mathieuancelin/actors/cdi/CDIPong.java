package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.api.ActorConfig;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ActorConfig("pong")
@ApplicationScoped
public class CDIPong extends CDIActor {
           
    public void listen(@Observes String evt) {
        if (evt.startsWith("ping")) {
            CDIActorsTest.down.countDown();
            sender().tell("pong", self());
            System.out.println("CDI PONG");
        } else if (evt.startsWith("stop")) {
            System.out.println("STOP CDI PONG");    
            context().stop(self());
        } else {
            System.err.println("[CDI PONG] Unknow message : " + evt);
        }
    }
}
