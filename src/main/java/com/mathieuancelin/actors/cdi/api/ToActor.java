package com.mathieuancelin.actors.cdi.api;

import java.lang.annotation.*;
import javax.inject.Qualifier;

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface ToActor {
    String value();
}
