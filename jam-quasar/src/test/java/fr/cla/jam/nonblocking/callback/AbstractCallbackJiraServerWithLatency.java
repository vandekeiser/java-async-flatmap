package fr.cla.jam.nonblocking.callback;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.exampledomain.CallbackJiraServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public abstract class AbstractCallbackJiraServerWithLatency implements CallbackJiraServer {
    //protected static final long MIN_SLEEP = 1000, MAX_SLEEP = 10_000;
    //protected static final long MIN_SLEEP = 1000, MAX_SLEEP = 3000;
    //protected static final long MIN_SLEEP = 0, MAX_SLEEP = 500;
    protected static final long MIN_SLEEP = 10, MAX_SLEEP = 500;
    //protected static final long MIN_SLEEP = 0, MAX_SLEEP = 1;

    protected static final Executor delayExecutor = Executors.newFixedThreadPool(1);

    private final CallbackJiraServer jira;
    protected final Map<Object, Long> sleeps = new HashMap<>();

    public AbstractCallbackJiraServerWithLatency(CallbackJiraServer jira) {
        this.jira = jira;
    }

    @Override
    public void findBundlesByName(String bundleName, Callback<Set<JiraBundle>> callback) {
        BiConsumer<String, Callback<Set<JiraBundle>>> instantCall = jira::findBundlesByName;
        BiConsumer<String, Callback<Set<JiraBundle>>> delayedCall = delay(instantCall);
        delayedCall.accept(bundleName, callback);
    }

    @Override
    public void findComponentsByBundle(JiraBundle bundle, Callback<Set<JiraComponent>> callback) {
        BiConsumer<JiraBundle, Callback<Set<JiraComponent>>> instantCall = jira::findComponentsByBundle;
        BiConsumer<JiraBundle, Callback<Set<JiraComponent>>> delayedCall = delay(instantCall);
        delayedCall.accept(bundle, callback);
    }

    private <I, O> BiConsumer<I, Callback<O>> delay(BiConsumer<I, Callback<O>> instant) {
        return (i, c) -> {
            instant.accept(i, new Callback<O>() {
                @Override
                public void onSuccess(O success) {
                    sleepThenPropagateSuccess(i, success, c);
                }

                @Override
                public void onFailure(Throwable failure) {
                    c.onFailure(failure);
                }
            });
        };
    }

    protected abstract <I, O> void sleepThenPropagateSuccess(I i, O success, Callback<O> c);

    protected long sleepDuration(Object request) {
        return sleeps.computeIfAbsent(
                request,
                k -> ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP)
        );
    }
}
