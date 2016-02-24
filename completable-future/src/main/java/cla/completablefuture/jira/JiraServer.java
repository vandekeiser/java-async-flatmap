package cla.completablefuture.jira;

import java.util.Set;

public interface JiraServer {
    
    Set<JiraBundle> findBundlesByName(String bundleName);
    
    Set<JiraComponent> findComponentsByBundle(JiraBundle bundle);
    
}
