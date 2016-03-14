package cla.completablefuture.jira.nonblocking.callback;

import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

import java.util.Set;

public interface CallbackJiraServer {
    
    Callback<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
 
}
