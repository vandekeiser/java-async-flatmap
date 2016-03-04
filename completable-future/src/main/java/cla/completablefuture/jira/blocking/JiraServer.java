package cla.completablefuture.jira.blocking;

import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;

import java.util.Set;

public interface JiraServer {
    
    Set<JiraBundle> findBundlesByName(String bundleName);
    
    Set<JiraComponent> findComponentsByBundle(JiraBundle bundle);
    
}
