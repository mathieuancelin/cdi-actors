package com.mathieuancelin.actors.cdi.api;

import akka.actor.ActorRef;
import java.lang.annotation.Annotation;
import javax.enterprise.util.TypeLiteral;

public interface ActorEvent<T> {

   public void fire(T event);
   
   public void fire(T event, ActorRef from);

   public ActorEvent<T> select(Annotation... qualifiers);
   
   public <U extends T> ActorEvent<U> select(Class<U> subtype, Annotation... qualifiers);

   public <U extends T> ActorEvent<U> select(TypeLiteral<U> subtype, Annotation... qualifiers);
}
