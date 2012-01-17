package com.mathieuancelin.actors.cdi;

import com.mathieuancelin.actors.cdi.api.*;
import com.mathieuancelin.actors.cdi.api.Messages.PoisonPill;
import com.mathieuancelin.actors.cdi.util.Functionnal.Action;
import com.mathieuancelin.actors.cdi.util.Functionnal.Option;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;

public class ActorImpl implements Runnable, LocalActorRef {

    private static class Message {

        Object payload;
        
        Option<ActorRef> sender;

        public Message(Object payload, Option<ActorRef> sender) {
            this.payload = payload;
            this.sender = sender;
        }
    }
    
    private ConcurrentLinkedQueue<Message> mailbox =
            new ConcurrentLinkedQueue<Message>();
    private final Action<Object> react;
    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean buzy = new AtomicBoolean(false);
    protected Option<ActorRef> sender = Option.none();
    protected final String uuid;
    private CountDownLatch latch = new CountDownLatch(1);
    private final Instance<Object> instance; 
    private final Class<?> internalBean;
    protected final Actors actors;
    
    private final List<Method> observers = new ArrayList<Method>();

    public ActorImpl(Instance<Object> instanceArg, Class<?> internalBeanArg, Actors actors) {
        uuid = UUID.randomUUID().toString();
        this.instance = instanceArg;
        this.internalBean = internalBeanArg;
        this.actors = actors;
        for (Method m : internalBeanArg.getDeclaredMethods()) {
            if (m.getParameterTypes().length == 1) {
                if (m.getParameterAnnotations()[0][0].annotationType().equals(Observes.class)) {
                    observers.add(m);
                }
            }
        }
        this.react = new Action<Object>() {
            public void apply(Object t) {
                for (Method m : observers) {
                    if (m.getParameterTypes()[0].isAssignableFrom(t.getClass())) {
                        try {
                            Object o = instance.select(internalBean).get();
                            Method call = o.getClass().getDeclaredMethod(m.getName(), m.getParameterTypes());
                            call.setAccessible(true);
                            call.invoke(o, t);
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }

                    }
                }
            }
        };
        actors.register(uuid, this);
    }
    
    public void unregister() {
        actors.unregister(uuid);
    }

    @Override
    public boolean buzy() {
        return buzy.get();
    }

    protected void before() {
    }

    protected void after() {
    }

    @Override
    public void run() {
        mailbox.clear();
        started.compareAndSet(false, true);
        before();
        act(react);
        after();
        started.compareAndSet(true, false);
    }

    public void act(Action<Object> react) {
        loop(react);
    }

    public final ActorImpl me() {
        return this;
    }

    private void waitIfMailboxIsEmpty() {
        if (mailbox.isEmpty()) {
            setLatchAndWait();
        }
    }

    private void setLatchAndWait() {
        if (latch.getCount() == 0) {
            latch = new CountDownLatch(1);
        }
        try {
            print("waiting for a message");
            latch.await();
            print("finished waiting for a message");
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        }
    }

    /**
     * Method to loop the reception function to read all messages in the mailbox
     * one by one. Once launch, the loop will end with the stopActor method or
     * with a poison pill.
     */
    public final void loop(Action<Object> react) {
        while (started.get()) {
            waitIfMailboxIsEmpty();
            print("polling message");
            Message ret = mailbox.poll();
            print("got message " + ret);
            if (ret != null) {
                if (ret.payload.getClass().equals(PoisonPill.class)) {
                    stopActor();
                } else {
                    print("processing new message " + ret.payload);
                    sender = ret.sender;
                    print("1");
                    buzy.compareAndSet(false, true);
                    print("2");
                    if (!sender.isEmpty()) {
                        Actors.currentSender.set(sender.get()); 
                    }
                    print("3");
                    print("ready to proccess react ");
                    react.apply(ret.payload);
                    print("react done ");
                    Actors.currentSender.remove();
                    buzy.compareAndSet(true, false);
                    sender = Option.none();
                    print("went through message processing ");
                }
            }
        }
    }
    
    public void print(String something) {
        //System.out.println(Thread.currentThread().getName() + " [" + id() + "] " + something);
    }

    @Override
    public final void send(Object msg) {
        if (msg != null) {
            mailbox.add(new Message(msg, Option.<ActorRef>none()));
            latch.countDown();
        }
    }

    @Override
    public final void send(Object msg, ActorRef from) {
        if (msg != null) {
            mailbox.add(new Message(msg, Option.maybe(from)));
            latch.countDown();
        }
    }

    @Override
    public final void send(Object msg, String from) {
        send(msg, actors.getActor(from));
    }

    @Override
    public void send(Object msg, ActorURL from) {
        send(msg, actors.getActor(from.name));
    }

    @Override
    public String id() {
        return uuid;
    }

    @Override
    public ActorURL asLocalURL() {
        return new LocalActorURL(Constants.HOST, Constants.PORT_STRING, uuid);
    }

    public final ActorImpl stopActor() {
        started.compareAndSet(true, false);
        return this;
    }

    public final ActorImpl startActor() {
        started.compareAndSet(false, true);
        Actors.getExecutor().submit(this);
        return this;
    }
}
