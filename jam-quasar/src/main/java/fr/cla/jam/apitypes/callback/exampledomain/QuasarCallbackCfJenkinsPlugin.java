package fr.cla.jam.apitypes.callback.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.*;
import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.Collections.emptySet;

public class QuasarCallbackCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);
    private final FiberExecutorScheduler dedicatedScheduler;

    public QuasarCallbackCfJenkinsPlugin(CallbackJiraApi srv, Executor dedicatedPool) {
        super(srv);
        this.dedicatedScheduler = dedicatedScheduler(dedicatedPool);

        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            CallbackCfAdapter.adapt(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CallbackCfAdapter.flatMapAdapt(
            bundles,
            srv::findComponentsByBundle,
            CallbackCfAdapter::adapt
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    private FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return new FiberExecutorScheduler("QuasarCallbackCfJenkinsPlugin scheduler-" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true);
    }


    @Override public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        Fiber<Set<JiraComponent>> f = new Fiber<>(dedicatedScheduler, () ->
            new CfFiberAsync<>(bundleName, findComponentsByBundleNameAsync).run()
        ).start();

        try {
            return f.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if(cause instanceof CompletionException) throw (CompletionException)cause;
            throw new RuntimeException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return emptySet();
        }
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
