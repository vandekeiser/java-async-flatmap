package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static fr.cla.jam.FakeApi.MAX_SLEEP;
import static fr.cla.jam.FakeApi.MIN_SLEEP;

public class LatentSyncJiraApi implements SyncJiraApi {

    private final SyncJiraApi jira;
    private final Map<Object, Long> sleeps = new HashMap<>();

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
