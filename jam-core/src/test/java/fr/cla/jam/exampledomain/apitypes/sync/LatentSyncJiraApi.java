package fr.cla.jam.exampledomain.apitypes.sync;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.util.FakeApi;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class LatentSyncJiraApi implements SyncJiraApi {

    private final SyncJiraApi jira;
    private final Map<Object, Long> sleeps = new ConcurrentHashMap<>();

    public LatentSyncJiraApi(SyncJiraApi jira) {
        this.jira = jira;
    }

    @Override
    public Set<JiraBundle> findBundlesByName(String bundleName) {
        sleepRandomly_butAlwaysTheSameDurationWhenPassedParam(bundleName);
        return jira.findBundlesByName(bundleName);
    }

    @Override
    public Set<JiraComponent> findComponentsByBundle(JiraBundle bundle) {
        sleepRandomly_butAlwaysTheSameDurationWhenPassedParam(bundle);
        return jira.findComponentsByBundle(bundle);
    }
    
    private void sleepRandomly_butAlwaysTheSameDurationWhenPassedParam(Object param) {
        sleep(sleeps.computeIfAbsent(
            param,
            k -> ThreadLocalRandom.current().nextLong(FakeApi.MIN_SLEEP, FakeApi.MAX_SLEEP)
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
        return String.format(
            "%s (%s)",
            getClass().getSimpleName(),
            jira.toString()
        );
    }

}
