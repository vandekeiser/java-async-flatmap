package fr.cla.jam.blocking.exampledomain;

import fr.cla.jam.util.containers.Sets;
import fr.cla.jam.blocking.CollectSyncCollectionApiIntoCf;
import fr.cla.jam.blocking.SyncApi2CfApi;
import fr.cla.jam.blocking.VertxSyncApi2CfApi;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class VertxCollectSyncCollectionApiCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public VertxCollectSyncCollectionApiCfJenkinsPlugin(SyncJiraApi srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            SyncApi2CfApi.asyncify(srv::findBundlesByName, VertxSyncApi2CfApi.supplyVertx());
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> CollectSyncCollectionApiIntoCf.flatMapCollectionAsync(
                bundles,
                srv::findComponentsByBundle,
                VertxSyncApi2CfApi.supplyVertx(),
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