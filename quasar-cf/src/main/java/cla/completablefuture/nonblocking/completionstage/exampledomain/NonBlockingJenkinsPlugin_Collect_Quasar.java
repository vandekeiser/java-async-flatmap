package cla.completablefuture.nonblocking.completionstage.exampledomain;

import cla.completablefuture.exampledomain.AsyncJenkinsPlugin;
import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;
import cla.completablefuture.nonblocking.completionstage.NonBlockingAsyncSets_Collect;
import cla.completablefuture.nonblocking.completionstage.NonBlockingJiraServer;
import cla.completablefuture.nonblocking.completionstage.Quasarify;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class NonBlockingJenkinsPlugin_Collect_Quasar implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public NonBlockingJenkinsPlugin_Collect_Quasar(NonBlockingJiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            Quasarify.<String, Set<JiraBundle>>usingPool(dedicatedPool)
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        //findComponentsByBundlesAsync = bundles -> AsyncSets_Collect.flatMapAsyncUsingPool(bundles, srv::findComponentsByBundle, dedicatedPool);
        findComponentsByBundlesAsync = bundles -> NonBlockingAsyncSets_Collect.flatMapAsync(
                bundles,
                srv::findComponentsByBundle,
                Quasarify.usingPool(dedicatedPool)
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
