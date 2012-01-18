package com.mathieuancelin.actors.cdi.api;

import akka.routing.RouterConfig;

public interface RouterConfiguration {
    RouterConfig getConfig() ;
    String routerName();
}
