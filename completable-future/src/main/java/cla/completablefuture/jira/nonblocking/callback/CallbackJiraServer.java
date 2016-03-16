package cla.completablefuture.jira.nonblocking.callback;

import java.util.Set;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

public interface CallbackJiraServer {
    
    Callback<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
 
}
