package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class CallbackCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {

    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public CallbackCfJenkinsPlugin(CallbackJiraApi srv, Executor dedicatedPool) {
        super(srv);
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            CallbackCfAdapter.adapt(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CallbackCfAdapter.flatMapAdapt(
            bundles,
            srv::findComponentsByBundle,
            CallbackCfAdapter::adapt
        );

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }

}
