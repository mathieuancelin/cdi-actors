package com.mathieuancelin.actors.cdi;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Option;
import com.mathieuancelin.actors.cdi.api.ActorEvent;
import com.mathieuancelin.actors.cdi.api.To;
import java.lang.annotation.Annotation;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

public class ActorProducers {
    
    @Produces
    ActorSystem getActorSystem(CDIActors actors) {
        return actors.getSystem();
    }
    
    @Produces @To("dummyValueIgnoredByCDI")
    ActorRef getActorRef(InjectionPoint ip, CDIActors actors) {
        Option<To> maybeTo = CDIActors.getAnnotation(ip.getQualifiers(), To.class);
        for (To to : maybeTo) {
            return actors.getSystem().actorFor(to.value());
        }
        throw new RuntimeException("Error, no To");
    } 
    
    @Produces @To("dummyValueIgnoredByCDI")
    <T> ActorEvent<T> getEvent(InjectionPoint ip, final Event<Object> evt) {
        Event e = evt;
        if (!ip.getQualifiers().isEmpty()) {
            e = e.select(ip.getQualifiers().toArray(new Annotation[ip.getQualifiers().size()]));
        }
        return new EventDecorator<T>(e, new ActorTarget(ip));
    } 

    private static class EventDecorator<T> implements ActorEvent<T> {
        
        private final Event event;

        private final ActorTarget target;
        
        public EventDecorator(Event event, ActorTarget target) {
            this.event = event;
            this.target = target;
        }

        public void fire(T evt) {
            event.select(ActorMessage.class).fire(new ActorMessage(evt, target, null));
        }
        
        public void fire(T evt, ActorRef from) {
            event.select(ActorMessage.class).fire(new ActorMessage(evt, target, from));
        }

        public ActorEvent<T> select(Annotation... qualifiers) {
            return new EventDecorator<T>(event.select(qualifiers), target);
        }

        public <U extends T> ActorEvent<U> select(Class<U> subtype, Annotation... qualifiers) {
            return new EventDecorator<U>(event.select(subtype, qualifiers), target);
        }

        public <U extends T> ActorEvent<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            return new EventDecorator<U>(event.select(subtype, qualifiers), target);
        }
    }
    
    public static class ActorMessage {
        
        private final Object payload;
    
        private final ActorTarget target;
        
        private final ActorRef from;

        public ActorMessage(Object payload, ActorTarget target, ActorRef from) {
            this.payload = payload;
            this.target = target;
            this.from = from;
        }

        public Object getPayload() {
            return payload;
        }

        public ActorTarget getTarget() {
            return target;
        }

        public ActorRef getFrom() {
            return from;
        }
    }
    
    public static class ActorTarget {
        
        private final InjectionPoint ip;

        public ActorTarget(InjectionPoint ip) {
            this.ip = ip;
        }

        public InjectionPoint getIp() {
            return ip;
        }
    }
}
