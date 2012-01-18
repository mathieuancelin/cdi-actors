package com.mathieuancelin.actors.cdi;

import akka.actor.*;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.FromActorEngine;
import com.mathieuancelin.actors.cdi.api.RouterConfiguration;
import com.mathieuancelin.actors.cdi.api.ToActor;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
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
    
    private RouterConfiguration config;
    
    private Class<?> clazz;
    
    final static Map<String, Boolean> routers = new HashMap<String, Boolean>();

    public CDIActor() {
        this.clazz = getClass();
        if (getClass().isAnnotationPresent(ActorConfig.class)) {
            name = getClass().getAnnotation(ActorConfig.class).value();
            if (!getClass().getAnnotation(ActorConfig.class).withRouter().equals(ActorConfig.NotRouterConfig.class)) {
                try {
                    config = (RouterConfiguration) getClass().getAnnotation(ActorConfig.class).withRouter().newInstance();
                } catch (Exception ex) {
                   ex.printStackTrace();
                }
            }
            if (name.equals(ActorConfig.DEFAULT_VALUE)) {
                name = null;
            }
        } /**else {
            name = getClass().getName();
        }**/
    }
    
    void createAndRegisterDelegateActor() {
        final CDIActor act = this;
        Props p = new Props(new UntypedActorFactory() {
            public Actor create() {
                delegate = new DelegateActor(events, name, act);
                return delegate;
            }
        });
        if (config != null) {
            boolean routerCreated = routers.containsKey(config.routerName()) ? true : false; 
            if(!routerCreated) {
                System.out.println("router not created for " + config.routerName());
                System.out.println(routers);
                routers.put(config.routerName(), true);
                System.out.println("put " + config.routerName());
                p = new Props(new UntypedActorFactory() {
                    public Actor create() {
                        CDIActor actor = (CDIActor) instances.select(clazz).get();
                        actor.start();
                        return actor.getDelegate();
                    }
                });
                p = p.withRouter(config.getConfig());
                delegateRef = actors.getSystem().actorOf(p, config.routerName());
                return;
            }
        }
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
    
    public void preStart() {
    }

    public void postStop() {
        if (config != null) {
            routers.remove(config.routerName());
        }
    }

    public void preRestart(Throwable reason, Option<Object> message) {
    }

    public void postRestart(Throwable reason) {
    }

    Actor getDelegate() {
        return delegate;
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
