package fr.cla.jam.apitypes.callback.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.apitypes.callback.CollectCallbackApiIntoCf;
import fr.cla.jam.apitypes.callback.QuasarCallbackApi2CfApi;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class QuasarCollectCallbackApiIntoCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);
    //private final static AtomicReference<FiberScheduler> callInFiberSchedulerCounter = new AtomicReference<>();

    public QuasarCollectCallbackApiIntoCfJenkinsPlugin(CallbackJiraApi srv, Executor dedicatedPool) {
        FiberScheduler dedicatedScheduler = dedicatedScheduler(dedicatedPool);

//        try {
//            System.out.println("SLEEP!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            Thread.sleep(Long.MAX_VALUE);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        
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

    private FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return new FiberExecutorScheduler("QuasarCollectCallbackApiIntoCfJenkinsPlugin scheduler-" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true);
    }

    //doesn't help, what is costly is the schedulers creating fibers
//    private FiberScheduler dedicatedScheduler(Executor dedicatedPool) {
//        return callInFiberSchedulerCounter.updateAndGet(fs -> {
//            if(fs != null) return fs;
//            else return new FiberExecutorScheduler("QuasarCollectCallbackApiIntoCfJenkinsPlugin scheduler", dedicatedPool, MonitorType.JMX, true);
//        });
//    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
