package cla.completablefuture.jenkins;

import cla.completablefuture.jira.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface AsyncJenkinsPlugin extends JenkinsPlugin {
    
    CompletableFuture<Set<JiraComponent>> findComponentsByBundleNameAsync(String bundleName);
    
    @Override default Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return findComponentsByBundleNameAsync(bundleName).join();
    }
    
}
