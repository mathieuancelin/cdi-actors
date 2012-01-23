package com.mathieuancelin.actors.cdi;

import akka.actor.*;
import com.mathieuancelin.actors.cdi.ActorsExtension.Tuple;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.FromActorEngine;
import com.mathieuancelin.actors.cdi.api.ToActor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import scala.Option;

public abstract class CDIActor {
    
    @Inject @Any Event<Object> events;
    
    @Inject @Any Instance<Object> instances;
    
    @Inject CDIActors actors;
    
    private DelegateActor delegate;
    
    private ActorRef delegateRef;
        
    private String name ;
            
    public CDIActor() {
        if (getClass().isAnnotationPresent(ActorConfig.class)) {
            name = getClass().getAnnotation(ActorConfig.class).value();
            if (name.equals(ActorConfig.DEFAULT_VALUE)) {
                name = null;
            }
        }
    }
    
    private void createAndRegisterDelegateActor() {
        final CDIActor act = this;
        Props p = new Props(new UntypedActorFactory() {
            public Actor create() {
                delegate = new DelegateActor(events, name, act);
                return delegate;
            }
        });
        if (name != null) {
            delegateRef = actors.getSystem().actorOf(p, name);
        } else {
            delegateRef = actors.getSystem().actorOf(p);
        }
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
    
    public void wakeUp() {}
    
    public void preStart() {}

    public void postStop() {}

    public void preRestart(Throwable reason, Option<Object> message) {}

    public void postRestart(Throwable reason) {}

    Actor getDelegate() {
        return delegate;
    }
    
    @PostConstruct
    void start() {
        createAndRegisterDelegateActor();
        System.out.println("Starting actor '" + (name == null ? "undefined" : name) 
                + "' (available at " + delegateRef.path().toString() + ")");
    }

    public static class DelegateActor extends UntypedActor {
        
        private final Event<Object> events;
        
        private final String name;
        
        private final CDIActor actor;
        
        private final List<Tuple<AnnotatedMethod, ObserverMethod>> observers;

        public DelegateActor(Event<Object> events, String name, CDIActor actor) {
            this.events = events;
            if (name == null) {
                this.name = "undefined";
            } else {
                this.name = name;
            }
            this.actor = actor;
            this.observers = ActorsExtension.observers.get(actor.getClass());
        }

        @Override
        public void onReceive(Object o) throws Exception {
            Class clazz = o.getClass();
//            events.select(new FromActorEngineAnnotation())
//                    .select(new ToActorAnnotation(name))
//                    .select(clazz).fire(get(o, clazz));
            
            // NASTY WORKAROUND. Not proud of it !!!!!   
            for (Tuple<AnnotatedMethod, ObserverMethod> m : observers) {
                if (typeMatch(((Class<?>) m._2.getObservedType()), clazz)) {
                    invoke(m._1.getJavaMember(), actor, o);
                }
            }
        }  
            
        private static boolean typeMatch(Class<?> from, Class<?> to) {
            return from.isAssignableFrom(to);
        }
        
        private static void invoke(Method m, Object on, Object with) {
            boolean accessible = m.isAccessible();
            try {
                if (!accessible) {
                    m.setAccessible(true);
                }
                m.invoke(on, with);
            } catch (Throwable ex) {
                ex.printStackTrace();
            } finally {
                if (!accessible) {
                    m.setAccessible(false);
                }
            }
        }

        @Override
        public void postStop() {
            actor.postStop();
        }

        @Override
        public void postRestart(Throwable reason) {
            actor.postRestart(reason);
        }

        @Override
        public void preStart() {
            actor.preStart();
        }

        @Override
        public void preRestart(Throwable reason, Option<Object> message) {
            actor.preRestart(reason, message);
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
