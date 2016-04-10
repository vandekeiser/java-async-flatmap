package fr.cla.jam.apitypes.promise.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.apitypes.promise.QuasarPromiseCfAdapter;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class QuasarPromiseCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {

    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    public QuasarPromiseCfJenkinsPlugin(PromiseJiraApi srv, Executor dedicatedPool) {
        super(srv);
        FiberScheduler dedicatedScheduler = dedicatedScheduler(dedicatedPool);

        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            QuasarPromiseCfAdapter.<String, Set<JiraBundle>>usingFiberScheduler(dedicatedScheduler)
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>>
        findComponentsByBundlesAsync = bundles -> PromiseCfAdapter.adaptFlatMap(
            bundles,
            srv::findComponentsByBundle,
            QuasarPromiseCfAdapter.usingFiberScheduler(dedicatedScheduler)
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    private FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return new FiberExecutorScheduler("QuasarPromiseCfJenkinsPlugin scheduler-" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true);
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }


}
