package cla.completablefuture.jenkins.nonblocking;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import cla.completablefuture.jenkins.AsyncJenkinsPlugin;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.nonblocking.NonBlockingJiraServer;
import cla.completablefuture.nonblocking.NonBlockingAsyncSets_Collect;
import cla.completablefuture.nonblocking.NonBlockingCompletableFutures;

public class NonBlockingJenkinsPlugin_Collect implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public NonBlockingJenkinsPlugin_Collect(NonBlockingJiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> 
                findBundlesByNameAsync = NonBlockingCompletableFutures.asyncifyUsingPool(srv::findBundlesByName, dedicatedPool);
        
        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync = 
            bundles -> NonBlockingAsyncSets_Collect.flatMapAsyncUsingPool(bundles, srv::findComponentsByBundle, dedicatedPool);

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    @Override
    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
