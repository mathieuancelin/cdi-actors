package com.mathieuancelin.actors.cdi.api;

public class LocalActorURL extends ActorURL {
    
    public LocalActorURL(String host, String port, String name) {
        super("local", host, port, name);
    }

    @Override
    public boolean isRemote() {
        return false;
    }
}
