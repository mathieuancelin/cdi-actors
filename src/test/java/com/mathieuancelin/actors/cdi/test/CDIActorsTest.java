package com.mathieuancelin.actors.cdi.test;

import akka.actor.ActorRef;
import akka.actor.Extension;
import com.mathieuancelin.actors.cdi.ActorProducers;
import com.mathieuancelin.actors.cdi.ActorsExtension;
import com.mathieuancelin.actors.cdi.CDIActors;
import com.mathieuancelin.actors.cdi.test.Pi.Calculate;
import com.mathieuancelin.actors.cdi.test.Pi.Master;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.ActorEvent;
import com.mathieuancelin.actors.cdi.api.To;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CDIActorsTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addPackage(ActorProducers.class.getPackage())
            .addPackage(ActorConfig.class.getPackage())
            .addPackage(Pi.Master.class.getPackage())
            .addPackage(Pi.class.getPackage())
            .addAsServiceProvider(Extension.class, ActorsExtension.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }
    
    public static final CountDownLatch down = new CountDownLatch(20);

    @Inject @To(actor=CDIPing.class) ActorRef ping;
    
    @Inject CDIActors actors;
        
    @Test
    public void testCDIActors() throws Exception {    
        ping.tell("start");
        down.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(down.getCount(), 0);
    }
    
    @Inject @To(actor=Master.class) ActorEvent<Calculate> master;

    @Test
    public void testPi() throws Exception {
        master.fire(new Calculate(4, 10000, 10000));
        Pi.latch.await(20, TimeUnit.SECONDS);
    }
}
