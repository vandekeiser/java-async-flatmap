package fr.cla.jam.exampledomain;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractCfJenkinsPlugin extends AbstractJenkinsPlugin implements CfJenkinsPlugin {

    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public AbstractCfJenkinsPlugin(
        JiraApi srv,
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync,
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync
    ) {
        super(srv);

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    @Override
    public final CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }

}
