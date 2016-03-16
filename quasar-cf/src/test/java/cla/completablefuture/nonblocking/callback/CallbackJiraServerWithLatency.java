package cla.completablefuture.nonblocking.callback;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.nonblocking.callback.BasicCompletableCallback;
import cla.completablefuture.jira.nonblocking.callback.Callback;
import cla.completablefuture.jira.nonblocking.callback.CallbackJiraServer;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;

public class CallbackJiraServerWithLatency implements CallbackJiraServer {
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 10_000;
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 3000;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 500;
    private static final long MIN_SLEEP = 10, MAX_SLEEP = 500;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 1;

    private final CallbackJiraServer jira;
    private final Map<Object, Long> sleeps = new HashMap<>();

    public CallbackJiraServerWithLatency(CallbackJiraServer jira) {
        this.jira = jira;
    }

    @Override
    public Callback<Set<JiraBundle>> findBundlesByName(String bundleName) {
        Function<String, Callback<Set<JiraBundle>>> instantCallbackProducer = jira::findBundlesByName;
        Function<String, Callback<Set<JiraBundle>>> delayedCallbackProducer = delay(instantCallbackProducer, bundleName);
        return delayedCallbackProducer.apply(bundleName);
    }

    @Override
    public Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        Function<JiraBundle, Callback<Set<JiraComponent>>> instantCallbackProducer = jira::findComponentsByBundle;
        Function<JiraBundle, Callback<Set<JiraComponent>>> delayedCallbackProducer = delay(instantCallbackProducer, bundle);
        return delayedCallbackProducer.apply(bundle);
    }

    private static final Executor delayExecutor = Executors.newCachedThreadPool();
    private static final FiberScheduler scheduler = new FiberExecutorScheduler("delay scheduler", delayExecutor);
    private <I, O> Function<I, Callback<O>> delay(Function<I, Callback<O>> instant, Object input) {
        return i -> {
            BasicCompletableCallback<O> delayed = new BasicCompletableCallback<>();

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
