package com.mathieuancelin.actors.cdi;

import akka.actor.ActorRef;
import akka.actor.Extension;
import com.mathieuancelin.actors.cdi.Pi.Calculate;
import com.mathieuancelin.actors.cdi.Pi.Master;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.ActorEvent;
import com.mathieuancelin.actors.cdi.api.SystemConfigurationEvent;
import com.mathieuancelin.actors.cdi.api.To;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
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

    @Inject @To("/user/ping") ActorRef ping;
    
    @Inject CDIActors actors;
    
    @Inject Master m;
    
    @Test
    public void testCDIActors() throws Exception {    
        ping.tell("start");
        down.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(down.getCount(), 0);
    }
    
    @Inject @To("/user/master") ActorEvent<Calculate> master;
    
    @Test
    public void testPi() throws Exception {
        master.fire(new Calculate(4, 10000, 10000));
        Pi.latch.await(200, TimeUnit.SECONDS);
    }
}
