package cla.completablefuture.jenkins;

import cla.completablefuture.AsyncCollections;
import cla.completablefuture.AsyncCollectionsWithGenericAsyncifier;
import cla.completablefuture.CompletableFutures;
import cla.completablefuture.Sets;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.JiraServer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public class JenkinsPlugin_GenericCollect_WithGenericAsyncifier implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public JenkinsPlugin_GenericCollect_WithGenericAsyncifier(JiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync = 
            CompletableFutures.asyncify(srv::findBundlesByName, QuasarCfAdapter.supplyQuasar());
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> AsyncCollectionsWithGenericAsyncifier.flatMapCollectionAsync(
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
