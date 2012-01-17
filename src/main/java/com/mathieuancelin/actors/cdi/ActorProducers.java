package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.api.ActorEvent;
import com.mathieuancelin.actors.cdi.api.ActorURL;
import com.mathieuancelin.actors.cdi.api.Sender;
import com.mathieuancelin.actors.cdi.api.To;
import java.lang.annotation.Annotation;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;

public class ActorProducers {
    
    @Produces @To
    <T> ActorEvent<T> getEvent(InjectionPoint ip, final Event<Object> evt) {
        Event e = evt;
        if (!ip.getQualifiers().isEmpty()) {
            e = e.select(ip.getQualifiers().toArray(new Annotation[ip.getQualifiers().size()]));
        }
        return new EventDecorator<T>(e, new Actors.ActorTarget(ip));
    } 
    
    private static class EventDecorator<T> implements ActorEvent<T> {
        
        private final Event event;

        private final Actors.ActorTarget target;
        
        public EventDecorator(Event event, Actors.ActorTarget target) {
            this.event = event;
            this.target = target;
        }

        public void fire(T evt) {
            Actors.currentTarget.set(target);
            event.select(ToActor.class).fire(new ToActor(evt, target));
            Actors.currentTarget.remove();
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
    
    @Produces
    Sender getSender(InjectionPoint ip) {
        return new Sender() {

            public String id() {
                return Actors.currentSender.get().id();
            }

            public boolean buzy() {
                return Actors.currentSender.get().buzy();
            }

            public void send(Object msg) {
                Actors.currentSender.get().send(msg);
            }

            public void send(Object msg, String from) {
                Actors.currentSender.get().send(msg, from);
            }

            public void send(Object msg, ActorURL from) {
                Actors.currentSender.get().send(msg, from);
            }

            public ActorURL asLocalURL() {
                return Actors.currentSender.get().asLocalURL();
            }
        };
    }
}
