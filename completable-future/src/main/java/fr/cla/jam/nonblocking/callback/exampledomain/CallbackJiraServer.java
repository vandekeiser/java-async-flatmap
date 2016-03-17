package fr.cla.jam.nonblocking.callback.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.Callback;

import java.util.Set;

public interface CallbackJiraServer {
    
    Callback<Set<JiraBundle>> findBundlesByName(String bundleName);
    
    Callback<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);
 
}
