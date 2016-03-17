package cla.completablefuture.blocking.exampledomain;

import cla.completablefuture.exampledomain.JiraBundle;
import cla.completablefuture.exampledomain.JiraComponent;

import java.util.Set;

public interface BlockingJiraServer {
    
    Set<JiraBundle> findBundlesByName(String bundleName);
    
    Set<JiraComponent> findComponentsByBundle(JiraBundle bundle);
    
}
