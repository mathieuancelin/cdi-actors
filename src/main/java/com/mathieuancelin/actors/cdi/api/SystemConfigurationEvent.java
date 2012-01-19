package com.mathieuancelin.actors.cdi.api;

import com.typesafe.config.Config;

public class SystemConfigurationEvent {
    private String systemName = "default";
    private Config systemConfig;
    private boolean enforceActorInjection = false;

    public SystemConfigurationEvent() {
    }

    public SystemConfigurationEvent errorOnActorInjection(boolean enforceActorInjection) {
        this.enforceActorInjection = enforceActorInjection;
        return this;
    }

    public SystemConfigurationEvent systemConfig(Config systemConfig) {
        this.systemConfig = systemConfig;
        return this;
    }

    public SystemConfigurationEvent systemName(String systemName) {
        this.systemName = systemName;
        return this;
    }

    public Config systemConfig() {
        return systemConfig;
    }

    public String systemName() {
        return systemName;
    }

    public boolean errorOnActorInjection() {
        return enforceActorInjection;
    }

    @Override
    public String toString() {
        return "SystemConfigurationEvent{" + "systemName=" + systemName + ", systemConfig=" + systemConfig + ", enforceActorInjection=" + enforceActorInjection + '}';
    }
}
