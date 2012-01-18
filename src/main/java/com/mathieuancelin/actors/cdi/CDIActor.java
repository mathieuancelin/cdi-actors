package com.mathieuancelin.actors.cdi;

import akka.actor.*;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.FromActorEngine;
import com.mathieuancelin.actors.cdi.api.ToActor;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

public abstract class CDIActor {
    
    @Inject @Any Event<Object> events;
    
    @Inject CDIActors actors;
    
    private DelegateActor delegate;
    
    private ActorRef delegateRef;
        
    private final String name;

    public CDIActor() {
        if (getClass().isAnnotationPresent(ActorConfig.class)) {
            name = getClass().getAnnotation(ActorConfig.class).value();
        } else {
            name = getClass().getName();
        }
    }
    
    void createAndRegisterDelegateActor() {
        final CDIActor act = this;
        delegateRef = actors.getSystem().actorOf(new Props(new UntypedActorFactory() {
            public Actor create() {
                delegate = new DelegateActor(events, name, act);
                return delegate;
            }
        }), name);
    }
    
    public ActorContext context() {
        return delegate.getContext();
    }

    public ActorRef self() {
        return delegate.getSelf();
    }

    public ActorRef sender() {
        return delegate.getSender();
    }   
    
    void start() {
        createAndRegisterDelegateActor();
        System.out.println("Starting " + name + " (available at " + delegateRef.path().toString() + ")");
    }

    public static class DelegateActor extends UntypedActor {
        
        private final Event<Object> events;
        
        private final String name;
        
        private final CDIActor actor;

        public DelegateActor(Event<Object> events, String name, CDIActor actor) {
            this.events = events;
            this.name = name;
            this.actor = actor;
        }

        @Override
        public void onReceive(Object o) throws Exception {
//            System.out.println("delegate actor for '" + name + "' received : " + o) ;
            Class clazz = o.getClass();
            events.select(new FromActorEngineAnnotation())
                    .select(new ToActorAnnotation(name))
                    .select(clazz).fire(get(o, clazz));
        }  
        
        private static <T> T get(Object o, Class<T> clazz) {
            return (T) o;
        }
    }
    
    public static class FromActorEngineAnnotation extends AnnotationLiteral<FromActorEngine>
                                    implements FromActorEngine {
        @Override
        public String toString() {
            return "FromActorEngineAnnotation{" + '}';
        }
    }
    
    public static class ToActorAnnotation extends AnnotationLiteral<ToActor>
                                    implements ToActor {
        
        private final String value;
        
        public ToActorAnnotation(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return "ToActorAnnotation{" + "value=" + value + '}';
        }        
    }
}
