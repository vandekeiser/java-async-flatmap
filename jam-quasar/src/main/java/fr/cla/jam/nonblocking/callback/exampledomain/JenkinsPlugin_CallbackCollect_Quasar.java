package fr.cla.jam.nonblocking.callback.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.CallbackAsyncSets_Collect;
import fr.cla.jam.nonblocking.callback.QuasarifyCallback;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class JenkinsPlugin_CallbackCollect_Quasar extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    public JenkinsPlugin_CallbackCollect_Quasar(CallbackJiraApi srv, Executor dedicatedPool) {
        FiberScheduler dedicatedScheduler = dedicatedScheduler(dedicatedPool);
        //Supplier<FiberScheduler> dedicatedScheduler = dedicatedSchedulerSupplier(dedicatedPool);

        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            QuasarifyCallback.<String, Set<JiraBundle>>usingFiberScheduler(dedicatedScheduler)
            //QuasarifyCallback.<String, Set<JiraBundle>>usingFiberSchedulerSupplier(dedicatedScheduler)
        .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CallbackAsyncSets_Collect.flatMapCallbackAsync(
            bundles,
            srv::findComponentsByBundle,
            //QuasarifyCallback.usingFiberSchedulerSupplier(dedicatedScheduler)
            QuasarifyCallback.usingFiberScheduler(dedicatedScheduler)
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    private Supplier<FiberScheduler> dedicatedSchedulerSupplier(Executor dedicatedPool) {
        return () -> dedicatedScheduler(dedicatedPool);
    }

    private FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return new FiberExecutorScheduler("JenkinsPlugin_CallbackCollect_Quasar scheduler-" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true);
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
