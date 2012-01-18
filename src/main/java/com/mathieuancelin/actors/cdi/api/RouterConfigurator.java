package com.mathieuancelin.actors.cdi.api;

import akka.routing.RouterConfig;
import com.mathieuancelin.actors.cdi.CDIActor;

public interface RouterConfigurator {
    RouterConfig getConfig() ;
    String routerName();
    Class<? extends CDIActor> actorOf();
}
