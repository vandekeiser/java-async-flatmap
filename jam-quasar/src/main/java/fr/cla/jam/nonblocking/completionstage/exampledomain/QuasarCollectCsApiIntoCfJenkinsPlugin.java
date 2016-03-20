package fr.cla.jam.nonblocking.completionstage.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.completionstage.CollectCsApiIntoCf;
import fr.cla.jam.nonblocking.completionstage.CsJiraApi;
import fr.cla.jam.nonblocking.completionstage.QuasarCsApi2CfApi;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class QuasarCollectCsApiIntoCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public QuasarCollectCsApiIntoCfJenkinsPlugin(CsJiraApi srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            QuasarCsApi2CfApi.<String, Set<JiraBundle>>usingPool(dedicatedPool)
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CollectCsApiIntoCf.flatMapAsync(
                bundles,
                srv::findComponentsByBundle,
                QuasarCsApi2CfApi.usingPool(dedicatedPool)
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}