package fr.cla.jam.nonblocking.promise.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.promise.CollectPromiseApiIntoCf;
import fr.cla.jam.nonblocking.promise.PromiseApi2CfApi;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class CollectPromiseApiIntoCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {

    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    public CollectPromiseApiIntoCfJenkinsPlugin(PromiseJiraApi srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            PromiseApi2CfApi.<String, Set<JiraBundle>>waitToBeCalledBack(dedicatedPool)
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>>
        findComponentsByBundlesAsync = bundles -> CollectPromiseApiIntoCf.flatMapPromiseAsync(
            bundles,
            srv::findComponentsByBundle,
                PromiseApi2CfApi.waitToBeCalledBack(dedicatedPool)
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }


}
