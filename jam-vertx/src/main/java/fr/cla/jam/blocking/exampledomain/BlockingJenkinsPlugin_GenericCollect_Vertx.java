package fr.cla.jam.blocking.exampledomain;

import fr.cla.jam.Sets;
import fr.cla.jam.blocking.BlockingAsyncCollectionsWithGenericAsyncifier;
import fr.cla.jam.blocking.BlockingCompletableFutures;
import fr.cla.jam.blocking.BlockingVertxCfAdapter;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.AsyncJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class BlockingJenkinsPlugin_GenericCollect_Vertx extends AbstractJenkinsPlugin implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public BlockingJenkinsPlugin_GenericCollect_Vertx(BlockingJiraApi srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            BlockingCompletableFutures.asyncify(srv::findBundlesByName, BlockingVertxCfAdapter.supplyVertx());
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> BlockingAsyncCollectionsWithGenericAsyncifier.flatMapCollectionAsync(
                    bundles,
                    srv::findComponentsByBundle,
                    BlockingVertxCfAdapter.supplyVertx(),
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
