package fr.cla.jam.nonblocking.callback.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.AsyncJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.QuasarifyCallback;
import fr.cla.jam.nonblocking.completionstage.NonBlockingAsyncSets_Collect;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class JenkinsPlugin_CallbackCollect_Quasar extends AbstractJenkinsPlugin implements AsyncJenkinsPlugin {
    
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
