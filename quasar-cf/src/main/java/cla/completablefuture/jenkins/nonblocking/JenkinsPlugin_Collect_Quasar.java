package cla.completablefuture.jenkins.nonblocking;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import cla.completablefuture.jenkins.AsyncJenkinsPlugin;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.nonblocking.JiraServer;
import cla.completablefuture.nonblocking.AsyncSets_Collect;
import cla.completablefuture.nonblocking.CompletableFutures;

public class JenkinsPlugin_Collect_Quasar implements AsyncJenkinsPlugin {
    
    private final Function<String, CompletableFuture<Set<JiraComponent>>> findComponentsByBundleNameAsync;

    public JenkinsPlugin_Collect_Quasar(JiraServer srv, Executor dedicatedPool) {
        Function<String, CompletableFuture<Set<JiraBundle>>> findBundlesByNameAsync =
            CompletableFutures.asyncify(srv::findBundlesByName, cla.completablefuture.jenkins.nonblocking.QuasarCfAdapter.supplyQuasar2(dedicatedPool));

        Function<Set<JiraBundle>, CompletableFuture<Set<JiraComponent>>> findComponentsByBundlesAsync =
                    bundles -> AsyncSets_Collect.flatMapAsync(bundles, srv::findComponentsByBundle, dedicatedPool);

        this.findComponentsByBundleNameAsync = findBundlesByNameAsync.andThen(
            bundlesFuture -> bundlesFuture.thenCompose(findComponentsByBundlesAsync)
        );
    }

    public CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName) {
        return findComponentsByBundleNameAsync.apply(bundleName);
    }
    
}
