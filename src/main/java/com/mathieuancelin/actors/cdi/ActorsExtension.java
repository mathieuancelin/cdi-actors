package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.api.Actor;
import com.mathieuancelin.actors.cdi.api.FromActorEngine;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.util.AnnotationLiteral;

@ApplicationScoped
public class ActorsExtension implements Extension {
    
    public static Set<Class<?>> classes = new HashSet<Class<?>>();
    
    public void observesListeners(@Observes ProcessObserverMethod evt) {
        if (evt.getAnnotatedMethod().getDeclaringType().getJavaClass().isAnnotationPresent(Actor.class)) {
//            System.out.println("class " + evt.getAnnotatedMethod().getDeclaringType().getJavaClass().getName() + " is an actor");
//            System.out.println("qualifiers are : " + evt.getObserverMethod().getObservedQualifiers());
            evt.getObserverMethod().getObservedQualifiers().add(new FromActorEngineAnnotation());
//            System.out.println("qualifiers now are : " + evt.getObserverMethod().getObservedQualifiers());
            classes.add(evt.getAnnotatedMethod().getDeclaringType().getJavaClass());
        }
    }
    
//    public void observesBeans(@Observes ProcessBean evt) {
////        if (evt.getBean().getBeanClass().isAnnotationPresent(Actor.class)) {
////            classes.add(evt.getBean().getBeanClass());
////        }
//        System.out.println("bean @ " + evt.getBean().getBeanClass());
//    }
    
    public static class FromActorEngineAnnotation extends AnnotationLiteral<FromActorEngine>
                                     implements FromActorEngine {}
}
