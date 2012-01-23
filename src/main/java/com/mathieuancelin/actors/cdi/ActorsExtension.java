package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.CDIActor.FromActorEngineAnnotation;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.RouterConfigurator;
import com.mathieuancelin.actors.cdi.api.SystemConfigurationEvent;
import com.typesafe.config.Config;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

@ApplicationScoped
public class ActorsExtension implements Extension {

    static Set<Class<? extends CDIActor>> classes = new HashSet<Class<? extends CDIActor>>();
    static Set<Class<? extends RouterConfigurator>> routers = new HashSet<Class<? extends RouterConfigurator>>();
    static Map<Class<?>, List<Tuple<AnnotatedMethod, ObserverMethod>>> observers = new HashMap<Class<?>, List<Tuple<AnnotatedMethod, ObserverMethod>>>();
    static String systemName = "default";
    static Config systemConfig;
    boolean enforceActorInjection = false;
        
    Set<InjectionPoint> ipts = new HashSet<InjectionPoint>();

    public void observesInjectionTarget(@Observes ProcessInjectionTarget<?> evt) {
        for (InjectionPoint ip : evt.getInjectionTarget().getInjectionPoints()) {
            ipts.add(ip);
        }
    }
    
    public void after(@Observes AfterDeploymentValidation evt, BeanManager mngr) {
        SystemConfigurationEvent config = new SystemConfigurationEvent();
        mngr.fireEvent(config);
        enforceActorInjection = config.errorOnActorInjection();
        systemConfig = config.systemConfig();
        systemName = config.systemName();
        if (enforceActorInjection) {
            for(InjectionPoint ip : ipts) {
                for (Class<?> c : classes) {
                    if (c.equals(ip.getType())) {
                        evt.addDeploymentProblem(
                            new DirectActorInjectionException("\nYou cannot inject an actor of type '" 
                            + ip.getType() + "'\ndirectly in bean '" 
                            + ip.getMember()
                            + "'.\nUse '@Inject @To(\"/user/" 
                            +  ((Class<?>) ip.getType()).getAnnotation(ActorConfig.class).value()
                            + "\") ActorRef " + ip.getMember().getName() + ";' instead."));
                    }
                }
            }
        }
    }
    
    public static class DirectActorInjectionException extends RuntimeException {

        public DirectActorInjectionException(String message) {
            super(message);
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
                evt.getObserverMethod().getObservedQualifiers().add(new CDIActor.ToActorAnnotation(name));
                if (!observers.containsKey(beanClass)) {
                    observers.put(beanClass, new ArrayList<Tuple<AnnotatedMethod, ObserverMethod>>());
                }
                observers.get(beanClass).add(new Tuple<AnnotatedMethod, ObserverMethod>(evt.getAnnotatedMethod(), evt.getObserverMethod()));
            } else {
                evt.addDefinitionError(new Throwable("You have to annotate your actors with @ActorConfig"));
            }
        }
    }

    public void observesBeans(@Observes ProcessBean evt) {
        Class<?> beanClass = evt.getBean().getBeanClass();
        if (CDIActor.class.isAssignableFrom(beanClass)) {
            String name = beanClass.getName();
            if (beanClass.isAnnotationPresent(ActorConfig.class)) {
                name = beanClass.getAnnotation(ActorConfig.class).value();
            }
            //System.out.println("Actor '" + name + "' found @ " + evt.getBean().getBeanClass());
        }
    }
    
    public void observesAnnotatedType(@Observes ProcessAnnotatedType evt) {
        Class<?> beanClass = evt.getAnnotatedType().getJavaClass();
        if (beanClass.getName().startsWith("akka.")) {
            evt.veto();
        }
    }
    
    public static class Tuple<A, B> {
        public final A _1;
        public final B _2;

        public Tuple(A _1, B _2) {
            this._1 = _1;
            this._2 = _2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Tuple<A, B> other = (Tuple<A, B>) obj;
            if (this._1 != other._1 && (this._1 == null || !this._1.equals(other._1))) {
                return false;
            }
            if (this._2 != other._2 && (this._2 == null || !this._2.equals(other._2))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (this._1 != null ? this._1.hashCode() : 0);
            hash = 47 * hash + (this._2 != null ? this._2.hashCode() : 0);
            return hash;
        }
    }
}
