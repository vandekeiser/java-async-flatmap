package cla.completablefuture.jenkins;

import java.util.Set;
import cla.completablefuture.jira.JiraComponent;

public interface JenkinsPlugin {
    
    Set<JiraComponent> findComponentsByBundleName(String bundleName);
    
}
