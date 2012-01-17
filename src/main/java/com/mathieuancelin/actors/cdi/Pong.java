package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.Actors;
import com.mathieuancelin.actors.cdi.Actors;
import com.mathieuancelin.actors.cdi.api.Actor;
import com.mathieuancelin.actors.cdi.api.Sender;
import static com.mathieuancelin.actors.cdi.util.PatternMatching.caseStartsWith;
import static com.mathieuancelin.actors.cdi.util.PatternMatching.with;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Actor
@ApplicationScoped
public class Pong {
    
    @Inject Actors actors;
    
    @Inject Sender sender;
       
    public void listen(@Observes String message) {
        for (String value : with(caseStartsWith("ping")).match(message)) {
            Ping.down.countDown();
            sender.send("pong");
            System.out.println("PONG");
        }
        for (String value : with(caseStartsWith("stop")).match(message)) {
            System.out.println("STOP PONG");    
            actors.stopAllActors();
        }
    }
}
