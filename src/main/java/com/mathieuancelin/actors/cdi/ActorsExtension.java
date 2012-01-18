package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.CDIActor.FromActorEngineAnnotation;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessObserverMethod;

@ApplicationScoped
public class ActorsExtension implements Extension {

    public static Set<Class<? extends CDIActor>> classes = new HashSet<Class<? extends CDIActor>>();

    public void observesListeners(@Observes ProcessObserverMethod evt) {
        Class<?> beanClass = evt.getAnnotatedMethod().getDeclaringType().getJavaClass();
        if (CDIActor.class.isAssignableFrom(beanClass)) {
            evt.getObserverMethod().getObservedQualifiers().add(new FromActorEngineAnnotation());
            String name = beanClass.getName();
            if (beanClass.isAnnotationPresent(ActorConfig.class)) {
                name = beanClass.getAnnotation(ActorConfig.class).value();
            }
            evt.getObserverMethod().getObservedQualifiers().add(new CDIActor.ToActorAnnotation(name));
            classes.add(evt.getAnnotatedMethod().getDeclaringType().getJavaClass());
        }
    }

    public void observesBeans(@Observes ProcessBean evt) {
        Class<?> beanClass = evt.getBean().getBeanClass();
        if (CDIActor.class.isAssignableFrom(beanClass)) {
            String name = beanClass.getName();
            if (beanClass.isAnnotationPresent(ActorConfig.class)) {
                name = beanClass.getAnnotation(ActorConfig.class).value();
            }
            System.out.println("Actor '" + name + "' found @ " + evt.getBean().getBeanClass());
        }
    }
    
    public void observesAnnotatedType(@Observes ProcessAnnotatedType evt) {
        Class<?> beanClass = evt.getAnnotatedType().getJavaClass();
        if (beanClass.getName().startsWith("akka.")) {
            evt.veto();
        }
    }
}
