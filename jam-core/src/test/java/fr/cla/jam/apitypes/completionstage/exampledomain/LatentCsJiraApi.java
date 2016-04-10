package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static fr.cla.jam.util.FakeApi.MAX_SLEEP;
import static fr.cla.jam.util.FakeApi.MIN_SLEEP;

public class LatentCsJiraApi implements CsJiraApi {

    private final CsJiraApi jira;
    private final Map<Object, Long> sleeps = new ConcurrentHashMap<>();

    public LatentCsJiraApi(CsJiraApi jira) {
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

    @Override public String description() {
        return getClass().getSimpleName();
    }

}
