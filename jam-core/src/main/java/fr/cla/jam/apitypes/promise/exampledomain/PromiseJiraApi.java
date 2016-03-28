package fr.cla.jam.apitypes.promise.exampledomain;

import fr.cla.jam.apitypes.promise.Promise;
import fr.cla.jam.exampledomain.JiraApi;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;

public interface PromiseJiraApi extends JiraApi {

    Promise<Set<JiraBundle>> findBundlesByName(String bundleName);

    Promise<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);

}
