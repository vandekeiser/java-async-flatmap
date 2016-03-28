package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.CallbackApi2CfApi;
import fr.cla.jam.apitypes.callback.CollectCallbackApiIntoCf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class CollectCallbackApiIntoCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {

    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public CollectCallbackApiIntoCfJenkinsPlugin(CallbackJiraApi srv, Executor dedicatedPool) {
        super(srv);
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            CallbackApi2CfApi.<String, Set<JiraBundle>>waitToBeCalledBack()
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> CollectCallbackApiIntoCf.flatMapCallbackAsync(
            bundles,
            srv::findComponentsByBundle,
            CallbackApi2CfApi.waitToBeCalledBack()
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
