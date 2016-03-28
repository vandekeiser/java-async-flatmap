package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.JiraApi;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;

public interface SyncJiraApi extends JiraApi {
    
    Set<JiraBundle> findBundlesByName(String bundleName);
    
    Set<JiraComponent> findComponentsByBundle(JiraBundle bundle);

}
