package cla.completablefuture.nonblocking.completionstage.exampledomain;

import cla.completablefuture.exampledomain.AsyncJenkinsPlugin;
import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;
import cla.completablefuture.nonblocking.completionstage.NonBlockingAsyncSets_Collect;
import cla.completablefuture.nonblocking.completionstage.NonBlockingCompletableFutures;
import cla.completablefuture.nonblocking.completionstage.NonBlockingJiraServer;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class NonBlockingJenkinsPlugin_Collect implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public NonBlockingJenkinsPlugin_Collect(NonBlockingJiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> 
                findBundlesByNameAsync = NonBlockingCompletableFutures.asyncifyUsingPool(srv::findBundlesByName, dedicatedPool);
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> NonBlockingAsyncSets_Collect.flatMapAsyncUsingPool(bundles, srv::findComponentsByBundle, dedicatedPool);

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
