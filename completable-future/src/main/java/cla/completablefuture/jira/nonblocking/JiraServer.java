package cla.completablefuture.jira.nonblocking;

import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface JiraServer {
    
    CompletionStage<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    CompletionStage<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);

}
