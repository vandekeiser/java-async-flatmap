package fr.cla.jam.nonblocking.exampledomain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.completionstage.NonBlockingJiraServer;

public class NonBlockingJiraServerWithLatency implements NonBlockingJiraServer {
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 10_000;
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 3000;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 500;
    private static final long MIN_SLEEP = 10, MAX_SLEEP = 500;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 1;
    private final NonBlockingJiraServer jira;
    private final Map<Object, Long> sleeps = new HashMap<>();

    public NonBlockingJiraServerWithLatency(NonBlockingJiraServer jira) {
        this.jira = jira;
    }

    private static final Executor delayExecutor = Executors.newCachedThreadPool();

    @Override
    public CompletableFuture<Set<JiraBundle>> findBundlesByName(String bundleName) {
        return CompletableFuture.runAsync(
            () -> sleepRandomlyForRequest(bundleName),
            delayExecutor
        ).thenCompose(
            _void -> jira.findBundlesByName(bundleName)
        );
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        return CompletableFuture.runAsync(
            () -> sleepRandomlyForRequest(bundle),
            delayExecutor
        ).thenCompose(
            _void -> jira.findComponentsByBundle(bundle)
        );
    }
    
    private void sleepRandomlyForRequest(Object request) {
        sleep(sleeps.computeIfAbsent(
            request,
            k -> ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP)
        ));
    }

    private void sleep(long sleepInMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
