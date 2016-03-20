package fr.cla.jam.nonblocking.callback.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.CollectCallbackApiIntoCf;
import fr.cla.jam.nonblocking.callback.QuasarCallbackApi2CfApi;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class QuasarCollectCallbackApiIntoCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    public QuasarCollectCallbackApiIntoCfJenkinsPlugin(CallbackJiraApi srv, Executor dedicatedPool) {
        FiberScheduler dedicatedScheduler = dedicatedScheduler(dedicatedPool);

        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            QuasarCallbackApi2CfApi.<String, Set<JiraBundle>>usingFiberScheduler(dedicatedScheduler)
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CollectCallbackApiIntoCf.flatMapCallbackAsync(
            bundles,
            srv::findComponentsByBundle,
            QuasarCallbackApi2CfApi.usingFiberScheduler(dedicatedScheduler)
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    private Supplier<FiberScheduler> dedicatedSchedulerSupplier(Executor dedicatedPool) {
        return () -> dedicatedScheduler(dedicatedPool);
    }

    private FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return new FiberExecutorScheduler("QuasarCollectCallbackApiIntoCfJenkinsPlugin scheduler-" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true);
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
