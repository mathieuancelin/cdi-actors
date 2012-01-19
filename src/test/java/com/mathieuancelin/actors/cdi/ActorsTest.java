package com.mathieuancelin.actors.cdi;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ActorsTest {

    public static final CountDownLatch down = new CountDownLatch(20);

    @Test
    public void shouldBeAbleToInjectCDI() throws Exception {
        final ActorSystem system = ActorSystem.create();
        final ActorRef pong = system.actorOf(new Props(Pong.class), "pong");
        final ActorRef ping = system.actorOf(new Props(Ping.class), "ping");       
        ping.tell("start");
        down.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(down.getCount(), 0);
        system.shutdown();
    }
}
