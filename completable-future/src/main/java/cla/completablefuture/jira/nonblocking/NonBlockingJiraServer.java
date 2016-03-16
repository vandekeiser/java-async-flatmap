package cla.completablefuture.jira.nonblocking;

import java.util.Set;
import java.util.concurrent.CompletionStage;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

public interface NonBlockingJiraServer {
    
    CompletionStage<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    CompletionStage<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
 
}
