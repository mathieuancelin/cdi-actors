package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.api.Actor;
import com.mathieuancelin.actors.cdi.api.ActorRef;
import com.mathieuancelin.actors.cdi.api.To;
import com.mathieuancelin.actors.cdi.util.Functionnal.Option;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
public class Actors {

    private static ExecutorService executor = Executors.newCachedThreadPool();
        
    @Inject @Any Instance<Object> instance;

    static {
        Runtime.getRuntime().addShutdownHook(
        new Thread() {

            @Override
            public void run() {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination(3600, TimeUnit.SECONDS);
                } catch (Throwable t) {
                    // nothing here
                }
            }
        });
    }
    
    private final static ConcurrentHashMap<String, ActorRef> actors =
            new ConcurrentHashMap<String, ActorRef>();
    
    @PostConstruct
    public void start() {
        for (Class<?> clazz : ActorsExtension.classes) {
            String name = clazz.getName();
            Actor actorAnnotation = clazz.getAnnotation(Actor.class);
            if (!actorAnnotation.value().equals(Actor.DEFAULT_VALUE)) {
                name = actorAnnotation.value();
            }
//            System.out.println("creating and starting " + name);
            new NamedActor(name, instance, clazz, this).startActor();
        }
    }
    
    ActorRef getActor(String name) {
        return actors.get(name);
    }

    void register(String name, ActorRef actor) {
        actors.putIfAbsent(name, actor);
    }

    void unregister(String name) {
        actors.remove(name);
    }

    Option<ActorRef> forName(String name) {
        return Option.maybe(getActor(name));
    }

    void shutdownAll() {
        executor.shutdown();
        executor.shutdownNow();
        executor = Executors.newCachedThreadPool();
    }

    static ExecutorService getExecutor() {
        return executor;
    }
    
    public <T> void listen(@Observes ToActor message) {
        ActorTarget target = message.getTarget();// TODO find target actor
        Class<?> actorFromClass = target.ip.getBean().getBeanClass();
        String nameFrom = actorFromClass.getName();
        if (actorFromClass.isAnnotationPresent(Actor.class)) {
            Actor actorAnnotation = actorFromClass.getAnnotation(Actor.class);
            if (!actorAnnotation.value().equals(Actor.DEFAULT_VALUE)) {
                nameFrom = actorAnnotation.value();
            }
        }
        if (actors.containsKey(nameFrom)) {
            Option<To> maybeTo = getAnnotation(target.ip.getQualifiers(), To.class);
            for (To to : maybeTo) {
                Option<ActorRef> maybeActor = Option.none();
                if (!to.name().equals("")) {
                    maybeActor = Option.maybe(actors.get(to.name()));
                }
                if (!to.value().equals(Object.class)) {
                    maybeActor = Option.maybe(actors.get(to.value().getName()));
                }
                for (ActorRef actor : maybeActor) {
                    actor.send(message.getPayload(), actors.get(nameFrom).asLocalURL());// TODO send to actual actor
                }
            }
        }
    }
    
    private static <T> Option<T> getAnnotation(Set<Annotation> annotations, Class<T> clazz) {
        for (Annotation anno : annotations) {
            if (anno.annotationType().equals(clazz)) {
                return Option.some((T) anno);
            }
        }
        return Option.none();
    }
    
    public void stopActor(String name) {
        if (actors.containsKey(name)) {
            ((ActorImpl) actors.get(name)).stopActor();
        }
    }
    
    public void startActor(String name) {
        if (actors.containsKey(name)) {
            ((ActorImpl) actors.get(name)).startActor();
        }
    }
    
    public void stopActor(Class clazz) {
        if (actors.containsKey(clazz.getName())) {
            ((ActorImpl) actors.get(clazz.getName())).stopActor();
        }
    }
    
    public void startActor(Class clazz) {
        if (actors.containsKey(clazz.getName())) {
            ((ActorImpl) actors.get(clazz.getName())).startActor();
        }
    }
    
    public void stopAllActors() {
        for(ActorRef ref : actors.values()) {
            ((ActorImpl) ref).stopActor();
        }
        shutdownAll();
    }
    
    public void startAllActors() {
        for(ActorRef ref : actors.values()) {
            ((ActorImpl) ref).startActor();
        }
    }
    
    static final ThreadLocal<ActorTarget> currentTarget = new ThreadLocal<ActorTarget>();
    
    static final ThreadLocal<ActorRef> currentSender = new ThreadLocal<ActorRef>();
    
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
