package fr.cla.jam.nonblocking.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.promise.exampledomain.PromiseJiraServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class PromiseJiraServerWithLatency implements PromiseJiraServer {

    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 10_000;
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 3000;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 500;
    private static final long MIN_SLEEP = 10, MAX_SLEEP = 500;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 1;

    private final PromiseJiraServer jira;
    private final Map<Object, Long> sleeps = new HashMap<>();

    public PromiseJiraServerWithLatency(PromiseJiraServer jira) {
        this.jira = jira;
    }

    @Override
    public Promise<Set<JiraBundle>> findBundlesByName(String bundleName) {
        Function<String, Promise<Set<JiraBundle>>> instantCallbackProducer = jira::findBundlesByName;
        Function<String, Promise<Set<JiraBundle>>> delayedCallbackProducer = delay(instantCallbackProducer, bundleName);
        return delayedCallbackProducer.apply(bundleName);
    }

    @Override
    public Promise<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        Function<JiraBundle, Promise<Set<JiraComponent>>> instantCallbackProducer = jira::findComponentsByBundle;
        Function<JiraBundle, Promise<Set<JiraComponent>>> delayedCallbackProducer = delay(instantCallbackProducer, bundle);
        return delayedCallbackProducer.apply(bundle);
    }

    private static final Executor delayExecutor = Executors.newCachedThreadPool();
    private static final FiberScheduler scheduler = new FiberExecutorScheduler("delay scheduler", delayExecutor);

    private <I, O> Function<I, Promise<O>> delay(Function<I, Promise<O>> instant, Object input) {
        return i -> {
            CompletablePromise<O> delayed = CompletablePromise.basic();

            instant.apply(i).whenComplete(
                r -> new Fiber<Void>(scheduler, () -> {
                    sleepRandomlyForRequest(input);
                    delayed.complete(r);
                }).start(),

                x -> delayed.completeExceptionnally(x)
            );

            return delayed;
        };
    }

    private void sleepRandomlyForRequest(Object request) throws SuspendExecution {
        sleep(sleeps.computeIfAbsent(
                request,
                k -> ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP)
        ));
    }

    private void sleep(long sleepInMillis) throws SuspendExecution {
        try {
            Fiber.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            Strand.currentStrand().interrupt();
        }
    }

}
