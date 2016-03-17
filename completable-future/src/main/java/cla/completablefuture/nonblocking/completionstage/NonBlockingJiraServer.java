package cla.completablefuture.nonblocking.completionstage;

import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface NonBlockingJiraServer {
    
    CompletionStage<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    CompletionStage<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
 
}
