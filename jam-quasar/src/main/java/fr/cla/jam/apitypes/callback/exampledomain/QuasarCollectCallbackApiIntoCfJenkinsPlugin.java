package fr.cla.jam.apitypes.callback.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.*;
import fr.cla.jam.apitypes.callback.CallbackApi2CfApi;
import fr.cla.jam.apitypes.callback.CollectCallbackApiIntoCf;
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

public class QuasarCollectCallbackApiIntoCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);
    private final FiberExecutorScheduler dedicatedScheduler;

    public QuasarCollectCallbackApiIntoCfJenkinsPlugin(CallbackJiraApi srv, Executor dedicatedPool) {
        super(srv);
        this.dedicatedScheduler = dedicatedScheduler(dedicatedPool);

        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            CallbackApi2CfApi.<String, Set<JiraBundle>>waitToBeCalledBack()
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CollectCallbackApiIntoCf.flatMapCallbackAsync(
            bundles,
            srv::findComponentsByBundle,
            CallbackApi2CfApi.waitToBeCalledBack()
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    private FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return new FiberExecutorScheduler("QuasarCollectCallbackApiIntoCfJenkinsPlugin scheduler-" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true);
    }


    @Override public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        CompletableFuture<Set<JiraComponent>> future = findComponentsByBundleNameAsync.apply(bundleName);

        //1. Direct CF join
//        return future.join();

        //2. CF join in a Fiber
//        Fiber<Set<JiraComponent>> f = new Fiber<>(dedicatedScheduler, ()->{
//            return future.join();
//        }).start();
//
//        try {
//            return f.get();
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return emptySet();
//        }

        //3. CF join in a FiberAsync
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
