package cla.completablefuture.jira.blocking;

import java.util.Set;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

public interface BlockingJiraServer {
    
    Set<JiraBundle> findBundlesByName(String bundleName);
    
    Set<JiraComponent> findComponentsByBundle(JiraBundle bundle);
    
}
