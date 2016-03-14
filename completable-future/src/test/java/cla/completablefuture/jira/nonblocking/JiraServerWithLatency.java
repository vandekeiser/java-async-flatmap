package cla.completablefuture.jira.nonblocking;

import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class JiraServerWithLatency implements JiraServer {
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 10_000;
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 3000;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 500;
    private static final long MIN_SLEEP = 10, MAX_SLEEP = 500;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 1;
    private final JiraServer jira;
    private final Map<Object, Long> sleeps = new HashMap<>();

    public JiraServerWithLatency(JiraServer jira) {
        this.jira = jira;
    }

    @Override
    public CompletableFuture<Set<JiraBundle>> findBundlesByName(String bundleName) {
        return CompletableFuture.runAsync(
                () -> sleepRandomlyForRequest(bundleName)
        ).thenCompose(
                _void -> jira.findBundlesByName(bundleName)
        );
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        //TODO attention les run async de pas les faire ds le FJP!!
        return CompletableFuture.runAsync(
            () -> sleepRandomlyForRequest(bundle)        
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
