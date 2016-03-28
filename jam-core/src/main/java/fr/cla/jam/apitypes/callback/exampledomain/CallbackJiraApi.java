package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.exampledomain.JiraApi;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;

public interface CallbackJiraApi extends JiraApi {
    
    void findBundlesByName(String bundleName, Callback<Set<JiraBundle>> callback);

    void findComponentsByBundle(JiraBundle bundle, Callback<Set<JiraComponent>> callback);

}
