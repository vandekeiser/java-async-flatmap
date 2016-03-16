package cla.completablefuture.jenkins.blocking;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import cla.completablefuture.blocking.BlockingAsyncSets_Collect;
import cla.completablefuture.blocking.BlockingCompletableFutures;
import cla.completablefuture.jenkins.AsyncJenkinsPlugin;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.blocking.BlockingJiraServer;

public class BlockingJenkinsPlugin_Collect implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public BlockingJenkinsPlugin_Collect(BlockingJiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync = 
            BlockingCompletableFutures.asyncifyWithPool(srv::findBundlesByName, dedicatedPool);
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> BlockingAsyncSets_Collect.flatMapAsync(bundles, srv::findComponentsByBundle, dedicatedPool);
                
        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
