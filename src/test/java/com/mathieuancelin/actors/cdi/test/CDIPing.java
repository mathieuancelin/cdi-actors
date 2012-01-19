package com.mathieuancelin.actors.cdi.test;

import com.mathieuancelin.actors.cdi.CDIActor;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.ActorEvent;
import com.mathieuancelin.actors.cdi.api.To;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ActorConfig("ping")
@ApplicationScoped
public class CDIPing extends CDIActor {
        
    @Inject @To("/user/pong") ActorEvent<String> pong;

    public void listen(@Observes String evt) {
        if (evt.startsWith("pong")) {
            System.out.println("CDI PING");
            if (CDIActorsTest.down.getCount() == 0) {
                pong.fire("stop", self());
                context().stop(self());
                System.out.println("STOP CDI PING");
            } else {
                pong.fire("ping", self());
            }
        } else if (evt.startsWith("start")) {
            pong.fire("ping", self());
            System.out.println("START CDI PING");
        }
    }
    
    public void listenAll(@Observes Object o) {
        System.err.println("[CDI PING] message received : " + o);
    }
}
