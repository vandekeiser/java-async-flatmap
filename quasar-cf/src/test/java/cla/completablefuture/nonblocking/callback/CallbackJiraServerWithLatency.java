package cla.completablefuture.nonblocking.callback;

import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.nonblocking.JiraServer;
import cla.completablefuture.jira.nonblocking.callback.Callback;
import cla.completablefuture.jira.nonblocking.callback.CallbackJiraServer;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
        Callback<Set<JiraBundle>> instant = jira.findBundlesByName(bundleName);
        
        return (onSuccess, onFailure) -> instant.handle(
            delayedSuccess(bundleName, onSuccess),
            onFailure
        );
    }

    @Override
    public Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        Callback<Set<JiraComponent>> instant = jira.findComponentsByBundle(bundle);

        return (onSuccess, onFailure) -> instant.handle(
            delayedSuccess(bundle, onSuccess),
            onFailure
        );
    }

    private <S, T> Consumer<T> delayedSuccess(S input, Consumer<T> instantConsumer) {
        return r -> {
            sleepRandomlyForRequest(input);
            instantConsumer.accept(r);
        };
    }
    
    private void sleepRandomlyForRequest(Object request) {
        sleep(sleeps.computeIfAbsent(
            request,
            k -> ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP)
        ));
    }

    private void sleep(long sleepInMillis) {
        try {
            //Strand.currentStrand().sleep(sleepInMillis);
            Fiber.sleep(sleepInMillis);
        } catch (SuspendExecution | InterruptedException e) {
            Strand.currentStrand().interrupt();
        }
    }
    
}
