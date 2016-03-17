package cla.completablefuture.nonblocking.callback.exampledomain;

import cla.completablefuture.exampledomain.AsyncJenkinsPlugin;
import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;
import cla.completablefuture.nonblocking.callback.QuasarifyCallback;
import cla.completablefuture.nonblocking.completionstage.NonBlockingAsyncSets_Collect;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class JenkinsPlugin_CallbackCollect_Quasar implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public JenkinsPlugin_CallbackCollect_Quasar(CallbackJiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            QuasarifyCallback.<String, Set<JiraBundle>>usingPool(dedicatedPool)
            .apply(srv::findBundlesByName);

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> 
        findComponentsByBundlesAsync = bundles -> NonBlockingAsyncSets_Collect.flatMapCallbackAsync(
                bundles,
                srv::findComponentsByBundle,
                QuasarifyCallback.usingPool(dedicatedPool)
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
