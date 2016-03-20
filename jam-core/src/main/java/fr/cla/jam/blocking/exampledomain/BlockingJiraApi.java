package fr.cla.jam.blocking.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;

public interface BlockingJiraApi {
    
    Set<JiraBundle> findBundlesByName(String bundleName);
    
    Set<JiraComponent> findComponentsByBundle(JiraBundle bundle);
    
}
