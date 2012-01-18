package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.CDIActor.FromActorEngineAnnotation;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.RouterConfigurator;
import com.mathieuancelin.actors.cdi.api.SystemConfigurationEvent;
import com.typesafe.config.Config;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

@ApplicationScoped
public class ActorsExtension implements Extension {

    static Set<Class<? extends CDIActor>> classes = new HashSet<Class<? extends CDIActor>>();
    static Set<Class<? extends RouterConfigurator>> routers = new HashSet<Class<? extends RouterConfigurator>>();

    static String systemName = "default";
    static Config systemConfig;
    boolean enforceActorInjection = false;
    
    Set<InjectionPoint> ipts = new HashSet<InjectionPoint>();
    
    // TODO configuration on actor (router, etc ....)
        
    public void start(@Observes BeforeBeanDiscovery evt, BeanManager mngr) {
        SystemConfigurationEvent config = new SystemConfigurationEvent();
        mngr.fireEvent(config);
        enforceActorInjection = config.isEnforceActorInjection();
        systemConfig = config.systemConfig();
        systemName = config.systemName();
    }
    
    public void observesInjectionTarget(@Observes ProcessInjectionTarget<?> evt) {
        if (enforceActorInjection) {
            for (InjectionPoint ip : evt.getInjectionTarget().getInjectionPoints()) {
                ipts.add(ip);
            }
        }
    }
    
    public void after(@Observes AfterDeploymentValidation evt) {
        if (enforceActorInjection) {
            for(InjectionPoint ip : ipts) {
                if (classes.contains(ip.getType())) {
                    evt.addDeploymentProblem(new Throwable("You cannot inject an actor of type " 
                            + ip.getType() + " direclyt in bean " 
                            + ip.getBean().getBeanClass().getName() 
                            + ". Use ActorRef injection."));
                }
            }
        }
    }
    
    public void observesListeners(@Observes ProcessObserverMethod evt) {
        Class<?> beanClass = evt.getAnnotatedMethod().getDeclaringType().getJavaClass();
        if (CDIActor.class.isAssignableFrom(beanClass)) {
            evt.getObserverMethod().getObservedQualifiers().add(new FromActorEngineAnnotation());
            String name = beanClass.getName();
            if (beanClass.isAnnotationPresent(ActorConfig.class)) {
                name = beanClass.getAnnotation(ActorConfig.class).value();
                if (!beanClass.getAnnotation(ActorConfig.class).withRouter().equals(ActorConfig.NotRouterConfig.class)) {
                    routers.add(beanClass.getAnnotation(ActorConfig.class).withRouter());
                } else {
                    classes.add(evt.getAnnotatedMethod().getDeclaringType().getJavaClass());
                }
            }
            evt.getObserverMethod().getObservedQualifiers().add(new CDIActor.ToActorAnnotation(name));
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
