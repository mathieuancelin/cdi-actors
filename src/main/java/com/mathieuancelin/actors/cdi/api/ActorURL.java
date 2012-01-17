package com.mathieuancelin.actors.cdi.api;

public abstract class ActorURL {
        
        public final String protocol;
        public final String host;
        public final String port;
        public final String name;

        public ActorURL(String protocol, String host, String port, String name) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.name = name;
        }
        
        public int port() {
            return Integer.valueOf(port);
        }

        @Override
        public String toString() {
            return protocol + "://" + host + ":" + port + "/" + name;
        }
        
        public abstract boolean isRemote();
        
        public static ActorURL fromString(String url) {
            url = url.replace("local://", "");
            String host = url.split(":")[0];
            String rest = url.split(":")[1];
            String port = rest.split("/")[0];
            String name = rest.split("/")[1];
            return new LocalActorURL(name, host, port);
        }
    }
