package fr.cla.jam.apitypes.promise.exampledomain;

import fr.cla.jam.apitypes.promise.CompletablePromise;
import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static fr.cla.jam.util.FakeApi.MAX_SLEEP;
import static fr.cla.jam.util.FakeApi.MIN_SLEEP;

public abstract class AbstractLatentPromiseJiraApi implements PromiseJiraApi {

    private final PromiseJiraApi jira;
    private final Map<Object, Long> sleeps = new ConcurrentHashMap<>();

    protected static final Executor delayExecutor = Executors.newCachedThreadPool();

    public AbstractLatentPromiseJiraApi(PromiseJiraApi jira) {
        this.jira = jira;
    }

    @Override
    public Promise<Set<JiraBundle>> findBundlesByName(String bundleName) {
        Function<String, Promise<Set<JiraBundle>>> instantCallbackProducer = jira::findBundlesByName;
        Function<String, Promise<Set<JiraBundle>>> delayedCallbackProducer = delay(instantCallbackProducer, bundleName);
        return delayedCallbackProducer.apply(bundleName);
    }

    @Override
    public Promise<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle) {
        Function<JiraBundle, Promise<Set<JiraComponent>>> instantCallbackProducer = jira::findComponentsByBundle;
        Function<JiraBundle, Promise<Set<JiraComponent>>> delayedCallbackProducer = delay(instantCallbackProducer, bundle);
        return delayedCallbackProducer.apply(bundle);
    }

    private <I, O> Function<I, Promise<O>> delay(Function<I, Promise<O>> instant, Object input) {
        return i -> {
            CompletablePromise<O> delayed = CompletablePromise.basic();

            instant.apply(i).whenComplete(
                    res -> sleepThenPropagateSuccess(i, res, delayed),

                    x -> delayed.completeExceptionnally(x)
            );

            return delayed;
        };
    }

    protected abstract <I, O> void sleepThenPropagateSuccess(I i, O success, CompletablePromise<O> c);

    protected long sleepDuration(Object request) {
        return sleeps.computeIfAbsent(
                request,
                k -> ThreadLocalRandom.current().nextLong(MIN_SLEEP, MAX_SLEEP)
        );
    }

}
