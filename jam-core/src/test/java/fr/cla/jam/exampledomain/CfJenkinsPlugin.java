package fr.cla.jam.exampledomain;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CfJenkinsPlugin extends JenkinsPlugin {
    
    CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName);
    
    @Override default Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return findComponentsByBundleNameAsync(bundleName).join();
    }
    
}
