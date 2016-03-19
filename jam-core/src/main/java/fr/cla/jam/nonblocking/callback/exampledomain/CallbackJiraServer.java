package fr.cla.jam.nonblocking.callback.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.Callback;

import java.util.Set;

public interface CallbackJiraServer {
    
    void findBundlesByName(String bundleName, Callback<Set<JiraBundle>> callback);

    void findComponentsByBundle(JiraBundle bundle, Callback<Set<JiraComponent>> callback);
 
}
