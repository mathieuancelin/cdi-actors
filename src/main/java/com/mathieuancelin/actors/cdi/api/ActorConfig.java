package com.mathieuancelin.actors.cdi.api;

import akka.routing.RouterConfig;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface ActorConfig {
    
    public static final String DEFAULT_VALUE = "#########";
    
    String value();
    
    Class<? extends RouterConfiguration> withRouter() default NotRouterConfig.class;
    
    public static class NotRouterConfig implements RouterConfiguration {

        public RouterConfig getConfig() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
