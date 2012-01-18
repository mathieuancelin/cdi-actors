package com.mathieuancelin.actors.cdi;

import akka.actor.ActorSystem;
import akka.japi.Option;
import com.mathieuancelin.actors.cdi.ActorProducers.ActorMessage;
import com.mathieuancelin.actors.cdi.api.To;
import java.lang.annotation.Annotation;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class CDIActors {
    
    @Inject @Any Instance<Object> instances;
    
    private final ActorSystem system;
        
    public CDIActors() {
        if (ActorsExtension.systemConfig == null) {
            this.system = ActorSystem.create(ActorsExtension.systemName);
        } else {
            this.system = ActorSystem.create(ActorsExtension.systemName, ActorsExtension.systemConfig);
        }
    }

    ActorSystem getSystem() {
        return system;
    }
    
    public void listen(@Observes ActorMessage message) {
        Option<To> maybeTo = getAnnotation(message.getTarget().getIp().getQualifiers(), To.class);
        for (To to : maybeTo) {
            system.actorFor(to.value()).tell(message.getPayload(), message.getFrom());
        }
    }
    
    public static <T> Option<T> getAnnotation(Set<Annotation> annotations, Class<T> clazz) {
        for (Annotation anno : annotations) {
            if (anno.annotationType().equals(clazz)) {
                return Option.some((T) anno);
            }
        }
        return Option.none();
    }
    
    public void shutdown() {
        system.shutdown();
    }
    
    @PostConstruct
    public void start() {
        for (Class<? extends CDIActor> clazz : ActorsExtension.classes) {
            instances.select(clazz).get().start();
        }
    }
}
