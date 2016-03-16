package cla.completablefuture.jenkins.blocking;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import cla.completablefuture.Sets;
import cla.completablefuture.blocking.BlockingAsyncCollectionsWithGenericAsyncifier;
import cla.completablefuture.blocking.BlockingCompletableFutures;
import cla.completablefuture.jenkins.AsyncJenkinsPlugin;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.blocking.BlockingJiraServer;

public class BlockingJenkinsPlugin_GenericCollect_Quasar implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public BlockingJenkinsPlugin_GenericCollect_Quasar(BlockingJiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync = 
            BlockingCompletableFutures.asyncify(srv::findBundlesByName, QuasarCfAdapter.supplyQuasar());
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> BlockingAsyncCollectionsWithGenericAsyncifier.flatMapCollectionAsync(
                    bundles,
                    srv::findComponentsByBundle,
                    QuasarCfAdapter.supplyQuasar(),
                    Collections::emptySet,
                    Sets::union
            );
                
        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
