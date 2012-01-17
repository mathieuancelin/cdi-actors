package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.Actors;
import com.mathieuancelin.actors.cdi.Actors;
import com.mathieuancelin.actors.cdi.api.Actor;
import com.mathieuancelin.actors.cdi.api.ActorEvent;
import com.mathieuancelin.actors.cdi.api.To;
import static com.mathieuancelin.actors.cdi.util.PatternMatching.caseStartsWith;
import static com.mathieuancelin.actors.cdi.util.PatternMatching.with;
import java.util.concurrent.CountDownLatch;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Actor
@ApplicationScoped
public class Ping {
        
    public static final CountDownLatch down = new CountDownLatch(20);
    
    @Inject @To(Pong.class) ActorEvent<String> msg;
        
    @Inject Actors actors;
    
    @PostConstruct
    public void start() {
        msg.fire("ping");
        System.out.println("PING");
    }
    
    public void listen(@Observes String message) {
        for (String value : with(caseStartsWith("pong")).match(message)) {
            System.out.println("PING");
            if (down.getCount() == 0) {
                msg.fire("stop");
                actors.stopActor(Ping.class);
                System.out.println("STOP PING");
            } else {
                msg.fire("ping");
            }
        }
    }
}
