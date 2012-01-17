package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.Actors.ActorTarget;

public class ToActor {
    
    private final Object payload;
    
    private final ActorTarget target;

    public ToActor(Object payload, ActorTarget target) {
        this.payload = payload;
        this.target = target;
    }

    public Object getPayload() {
        return payload;
    }

    public ActorTarget getTarget() {
        return target;
    }
}
