package com.mathieuancelin.actors.cdi.api;

import akka.routing.RouterConfig;
import com.mathieuancelin.actors.cdi.CDIActor;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface ActorConfig {
    
    public static final String DEFAULT_VALUE = "#########";
    
    String value() default DEFAULT_VALUE;
    
    Class<? extends RouterConfigurator> withRouter() default NotRouterConfig.class;
    
    public static class NotRouterConfig implements RouterConfigurator {

        public RouterConfig getConfig() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String routerName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Class<? extends CDIActor> actorOf() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
