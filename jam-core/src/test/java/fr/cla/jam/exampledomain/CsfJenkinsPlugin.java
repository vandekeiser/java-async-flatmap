package fr.cla.jam.exampledomain;

import fr.cla.jam.CfOfSet;

import java.util.Set;

public interface CsfJenkinsPlugin extends JenkinsPlugin {
    
    CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName);
    
    @Override default Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return findComponentsByBundleNameAsync(bundleName).join();
    }
    
}
