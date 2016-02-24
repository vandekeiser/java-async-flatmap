package cla.completablefuture.jenkins;

import cla.completablefuture.jira.JiraComponent;

import java.util.Set;

public interface JenkinsPlugin {
    
    Set<JiraComponent> findComponentsByBundleName(String bundleName);
    
}
