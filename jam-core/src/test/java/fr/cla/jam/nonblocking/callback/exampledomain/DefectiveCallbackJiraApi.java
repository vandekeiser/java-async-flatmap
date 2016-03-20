package fr.cla.jam.nonblocking.callback.exampledomain;

import fr.cla.jam.exampledomain.JiraApiException;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.Callback;

import java.util.Set;

public class DefectiveCallbackJiraApi implements CallbackJiraApi {

    @Override
    public void findBundlesByName(String bundleName, Callback<Set<JiraBundle>> callback) {
        callback.onFailure(new JiraApiException());
    }

    @Override
    public void findComponentsByBundle(JiraBundle bundle, Callback<Set<JiraComponent>> callback) {
        callback.onFailure(new JiraApiException());
    }

}
