package fr.cla.jam.nonblocking.promise.exampledomain;

import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.promise.Promise;

import java.util.Set;

public interface PromiseJiraServer {

    Promise<Set<JiraBundle>> findBundlesByName(String bundleName);

    Promise<Set<JiraComponent>> findComponentsByBundle(JiraBundle bundle);

}
