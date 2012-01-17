package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.api.Actor;
import com.mathieuancelin.actors.cdi.util.Functionnal;
import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.spi.Extension;
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
public class ActorsTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addPackage(ActorImpl.class.getPackage())
            .addPackage(Actor.class.getPackage())
            .addPackage(Functionnal.class.getPackage())
            .addAsServiceProvider(Extension.class, ActorsExtension.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }
    
    @Inject Actors actors;
    
    @Inject Ping ping;

//    public Weld weld;
//    public WeldContainer container;
//    public Instance<Object> instance;
//
//    @Before
//    public void start() {
//        weld = new Weld();
//        container = weld.initialize();
//        instance = container.instance();
//    }
//
//    @After
//    public void stop() {
//        weld.shutdown();
//    }
    
    @Test
    public void shouldBeAbleToInjectCDI() throws Exception {
//        Actors actors = instance.select(Actors.class).get();
//        Ping ping = instance.select(Ping.class).get();
        actors.toString();
        ping.toString();
        Ping.down.await(10, TimeUnit.SECONDS);
        Assert.assertEquals(Ping.down.getCount(), 0);
        actors.shutdownAll();
    }
}
