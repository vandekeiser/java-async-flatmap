package fr.cla.jam.blocking.exampledomain;

import fr.cla.jam.blocking.BlockingAsyncSets_Reduce;
import fr.cla.jam.blocking.BlockingCompletableFutures;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.AsyncJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class BlockingJenkinsPlugin_Reduce extends AbstractJenkinsPlugin implements AsyncJenkinsPlugin {
    
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
