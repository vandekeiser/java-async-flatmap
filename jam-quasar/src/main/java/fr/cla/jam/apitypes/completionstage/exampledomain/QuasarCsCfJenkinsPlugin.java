package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.apitypes.completionstage.QuasarCsCfAdapter;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class QuasarCsCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public QuasarCsCfJenkinsPlugin(CsJiraApi srv, Executor dedicatedPool) {
        super(srv);
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            QuasarCsCfAdapter.<String, Set<JiraBundle>>usingPool(dedicatedPool)
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CsCfAdapter.flatMapAdapt(
                bundles,
                srv::findComponentsByBundle,
                QuasarCsCfAdapter.usingPool(dedicatedPool)
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
