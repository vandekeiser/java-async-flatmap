package fr.cla.jam.exampledomain;

import fr.cla.jam.Csf;

import java.util.Set;

public interface CsfJenkinsPlugin extends JenkinsPlugin {
    
    Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName);
    
    @Override default Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return findComponentsByBundleNameAsync(bundleName).join();
    }
    
}
