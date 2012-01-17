package com.mathieuancelin.actors.cdi.api;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface Actor {
    
    public static final String DEFAULT_VALUE = "#########";
    
    String value() default DEFAULT_VALUE;
    boolean autostart() default true;
}
