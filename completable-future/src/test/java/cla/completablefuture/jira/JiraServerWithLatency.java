package cla.completablefuture.jira;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    public Set<JiraBundle> findBundlesByName(String bundleName) {
        sleepRandomlyForRequest(bundleName);
        return jira.findBundlesByName(bundleName);
    }

    @Override
    public Set<JiraComponent> findComponentsByBundle(JiraBundle bundle) {
        sleepRandomlyForRequest(bundle);
        return jira.findComponentsByBundle(bundle);
    }
    
    private void sleepRandomlyForRequest(Object request) {
        sleep(sleeps.computeIfAbsent(
                request,
                k -> ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP)
        ));
        //sleep(ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP));
    }

    private void sleep(long sleepInMillis) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
