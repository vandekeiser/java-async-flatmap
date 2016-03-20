package fr.cla.jam.blocking.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

public class ParallelStreamSyncApiJenkinsPlugin extends AbstractJenkinsPlugin implements JenkinsPlugin {

    private final Function<String, Set<JiraComponent>> findComponentsByBundleName;

    public ParallelStreamSyncApiJenkinsPlugin(SyncJiraApi srv, Executor dedicatedPool) {
        findComponentsByBundleName = bundleName -> srv.findBundlesByName(bundleName)
            .stream().parallel()
            .flatMap(b -> srv.findComponentsByBundle(b).stream())
            .collect(toSet());
    }

    public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return findComponentsByBundleName.apply(bundleName);
    }

}
