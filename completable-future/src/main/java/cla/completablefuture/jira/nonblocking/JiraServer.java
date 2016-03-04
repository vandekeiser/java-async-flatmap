package cla.completablefuture.jira.nonblocking;

import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface JiraServer {
    
    CompletableFuture<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    CompletableFuture<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
    
}
