package fr.cla.jam.blocking.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.Executor;

import static java.util.stream.Collectors.toSet;

public class BlockingJenkinsPlugin_ParallelStream extends AbstractJenkinsPlugin implements JenkinsPlugin {

    private final BlockingJiraApi srv;

    public BlockingJenkinsPlugin_ParallelStream(BlockingJiraApi srv, Executor dedicatedPool) {
        this.srv = srv;
    }

    public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return srv.findBundlesByName(bundleName)
            .stream().parallel()
            .flatMap(b -> srv.findComponentsByBundle(b).stream())
            .collect(toSet());
    }

}
