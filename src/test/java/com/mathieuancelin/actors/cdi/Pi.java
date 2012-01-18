package com.mathieuancelin.actors.cdi;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;
import com.mathieuancelin.actors.cdi.api.ActorConfig;
import com.mathieuancelin.actors.cdi.api.SystemConfigurationEvent;
import java.util.concurrent.CountDownLatch;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

public class Pi {
    
    public static final CountDownLatch latch = new CountDownLatch(1);
    
    public static class Calculate {
        public final int nrOfWorkers;
        public final int nrOfElements;
        public final int nrOfMessages;
        public Calculate(int nrOfWorkers, int nrOfElements, int nrOfMessages) {
            this.nrOfWorkers = nrOfWorkers;
            this.nrOfElements = nrOfElements;
            this.nrOfMessages = nrOfMessages;
        }
        @Override
        public String toString() {
            return "Calculate{" + "nrOfWorkers=" + nrOfWorkers + ", nrOfElements=" + nrOfElements + ", nrOfMessages=" + nrOfMessages + '}';
        }
    }

    public static class Work {
        public final int start;
        public final int nrOfElements;
        public Work(int start, int nrOfElements) {
            this.start = start;
            this.nrOfElements = nrOfElements;
        }
    }

    public static class Result {
        public final double value;
        public Result(double value) {
            this.value = value;
        }
    }

    public static class Worker extends UntypedActor {

        private double calculatePiFor(int start, int nrOfElements) {
            double acc = 0.0;
            for (int i = start * nrOfElements; i <= ((start + 1) * nrOfElements - 1); i++) {
                acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
            }
            return acc;
        }

        public void onReceive(Object message) {
            if (message instanceof Work) {
                Work work = (Work) message;
                double result = calculatePiFor(work.start, work.nrOfElements);
                getSender().tell(new Result(result));
            } else {
                throw new IllegalArgumentException("Unknown message [" + message + "]");
            }
        }
    }
    
    @ActorConfig("master")
    @ApplicationScoped
    public static class Master extends CDIActor {
        private int nrOfMessages;
        private int nrOfElements;
        private double pi;
        private int nrOfResults;
        private long start;
        private ActorRef router;

        public void listenCalculate(@Observes Calculate message) {
            this.nrOfMessages = message.nrOfMessages;
            this.nrOfElements = message.nrOfElements;
            System.out.println(message);
            router = context().actorOf(
                new Props(Worker.class).withRouter(new RoundRobinRouter(message.nrOfWorkers)), "pi");
            for (int start = 0; start < nrOfMessages; start++) {
                router.tell(new Work(start, nrOfElements), self());
            }
        }
        
        public void listenResult(@Observes Result result) {
            pi += result.value;
            nrOfResults += 1;
            if (nrOfResults == nrOfMessages) context().stop(self());
        } 

        @Override
        public void preStart() {
            System.out.println("Starting Pi computation");
            start = System.currentTimeMillis();
        }

        @Override
        public void postStop() {
            System.out.println(String.format(
                "\n\tPi estimate: \t\t%s\n\tCalculation time: \t%s millis",
                pi, (System.currentTimeMillis() - start)));
            latch.countDown();
        }
    }
    
    public static class Configurator {
        public void config(@Observes SystemConfigurationEvent evt) {
            System.out.println("config");
            evt.enforceActorInjection(true);
        }
    }
}