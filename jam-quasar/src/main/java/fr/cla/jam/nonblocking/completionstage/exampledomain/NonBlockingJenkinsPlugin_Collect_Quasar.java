package fr.cla.jam.nonblocking.completionstage.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.AsyncJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.completionstage.NonBlockingAsyncSets_Collect;
import fr.cla.jam.nonblocking.completionstage.CompletionStageJiraApi;
import fr.cla.jam.nonblocking.completionstage.Quasarify;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class NonBlockingJenkinsPlugin_Collect_Quasar extends AbstractJenkinsPlugin implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public NonBlockingJenkinsPlugin_Collect_Quasar(CompletionStageJiraApi srv, Executor dedicatedPool) {
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
