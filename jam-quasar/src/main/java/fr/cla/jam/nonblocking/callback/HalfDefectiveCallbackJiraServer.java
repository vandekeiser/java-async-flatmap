package fr.cla.jam.nonblocking.callback;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.exampledomain.JiraServerException;
import fr.cla.jam.nonblocking.callback.exampledomain.CallbackJiraServer;

import java.util.Set;

import static java.util.Collections.singleton;

public class HalfDefectiveCallbackJiraServer implements CallbackJiraServer {
    @Override
    public void findBundlesByName(String bundleName, Callback<Set<JiraBundle>> callback) {
        callback.onSuccess(singleton(new JiraBundle("foo")));
    }

    @Override
    public void findComponentsByBundle(JiraBundle bundle, Callback<Set<JiraComponent>> callback) {
        callback.onFailure(new JiraServerException());
    }
}
