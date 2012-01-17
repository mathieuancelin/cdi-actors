package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.api.ActorURL;
import com.mathieuancelin.actors.cdi.api.Constants;
import com.mathieuancelin.actors.cdi.api.LocalActorURL;
import javax.enterprise.inject.Instance;

public class NamedActor extends ActorImpl {

    protected final String name;
        
    public NamedActor(String name, Instance<Object> instanceArg, Class<?> internalBeanArg, Actors actors) {
        super(instanceArg, internalBeanArg, actors);
        this.name = name;
        actors.unregister(uuid);
        actors.register(name, this);
    }

    public String name() {
        return name;
    }

    @Override
    public void unregister() {
        actors.unregister(name);
    }

    @Override
    public ActorURL asLocalURL() {
        return new LocalActorURL(Constants.HOST, Constants.PORT_STRING, name);
    }

    @Override
    public String id() {
        return name;
    }
}
