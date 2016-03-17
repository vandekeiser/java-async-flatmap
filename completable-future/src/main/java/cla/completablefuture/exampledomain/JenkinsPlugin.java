package cla.completablefuture.exampledomain;

import java.util.Set;

public interface JenkinsPlugin {
    
    Set<JiraComponent> findComponentsByBundleName(String bundleName);
    
}
