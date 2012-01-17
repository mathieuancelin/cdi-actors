package com.mathieuancelin.actors.cdi.api;

import com.mathieuancelin.actors.cdi.util.Functionnal.Option;
import java.io.Serializable;

public class Messages {
    
    public static class MessageDelivered implements Serializable {}

    public static class MessageUndelivered implements Serializable {}
    
    public static class PoisonPill implements Serializable {}

    public static class Kill implements Serializable {}
    
    public static class Broadcast implements Serializable {

        private final Object message;
        
        private final Option<ActorRef> from;

        public Broadcast(Object message, Option<ActorRef> from) {
            this.message = message;
            this.from = from;
        }

        public Broadcast(Object message) {
            this.message = message;
            this.from = Option.none();
        }

        public Option<ActorRef> from() {
            return from;
        }

        public Object message() {
            return message;
        }
    }
}
