package fr.cla.jam.exampledomain;

import java.util.Set;

public interface JenkinsPlugin {
    
    Set<JiraComponent> findComponentsByBundleName(String bundleName);
    
}
