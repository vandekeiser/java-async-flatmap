package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

public class SeqStreamJenkinsPlugin extends AbstractJenkinsPlugin implements JenkinsPlugin {

    private final Function<String, Set<JiraComponent>> findComponentsByBundleName;

    public SeqStreamJenkinsPlugin(SyncJiraApi srv) {
        super(srv);
        findComponentsByBundleName = bundleName -> srv.findBundlesByName(bundleName)
            .stream()
            .flatMap(b -> srv.findComponentsByBundle(b).stream())
            .collect(toSet());
    }

    public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return findComponentsByBundleName.apply(bundleName);
    }

}
