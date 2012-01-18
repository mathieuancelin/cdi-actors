package com.mathieuancelin.actors.cdi.api;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface ActorConfig {
    
    public static final String DEFAULT_VALUE = "#########";
    
    String value();
}
