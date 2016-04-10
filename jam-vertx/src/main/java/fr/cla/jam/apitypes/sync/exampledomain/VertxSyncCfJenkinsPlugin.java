package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.CollectionSyncCfAdapter;
import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.apitypes.sync.VertxSyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class VertxSyncCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public VertxSyncCfJenkinsPlugin(SyncJiraApi srv, Executor dedicatedPool) {
        super(srv);
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            SyncCfAdapter.adapt(srv::findBundlesByName, VertxSyncCfAdapter.supplyVertx());
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> CollectionSyncCfAdapter.flatMapAdapt(
                bundles,
                srv::findComponentsByBundle,
                VertxSyncCfAdapter.supplyVertx(),
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
