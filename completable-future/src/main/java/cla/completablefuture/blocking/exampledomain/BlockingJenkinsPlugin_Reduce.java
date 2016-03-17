package cla.completablefuture.blocking.exampledomain;

import cla.completablefuture.blocking.BlockingAsyncSets_Reduce;
import cla.completablefuture.blocking.BlockingCompletableFutures;
import cla.completablefuture.exampledomain.AsyncJenkinsPlugin;
import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class BlockingJenkinsPlugin_Reduce implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public BlockingJenkinsPlugin_Reduce(BlockingJiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync = 
            BlockingCompletableFutures.asyncifyWithPool(srv::findBundlesByName, dedicatedPool);
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> BlockingAsyncSets_Reduce.flatMapAsync(bundles, srv::findComponentsByBundle, dedicatedPool);
                
        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
