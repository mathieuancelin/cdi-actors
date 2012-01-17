package com.mathieuancelin.actors.cdi.api;

import java.lang.annotation.*;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface To {
    @Nonbinding String name() default "";
    @Nonbinding Class value() default Object.class;
}
