package fr.cla.jam.blocking.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BlockingJiraServerWithLatency implements BlockingJiraServer {
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 10_000;
    //private static final long MIN_SLEEP = 1000, MAX_SLEEP = 3000;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 500;
    private static final long MIN_SLEEP = 10, MAX_SLEEP = 500;
    //private static final long MIN_SLEEP = 0, MAX_SLEEP = 1;
    private final BlockingJiraServer jira;
    private final Map<Object, Long> sleeps = new HashMap<>();

    public BlockingJiraServerWithLatency(BlockingJiraServer jira) {
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
