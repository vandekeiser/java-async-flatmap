package cla.completablefuture.blocking.exampledomain;

import cla.completablefuture.exampledomain.JenkinsPlugin;
import cla.completablefuture.exampledomain.JiraComponent;

import java.util.Set;
import java.util.concurrent.Executor;

import static java.util.stream.Collectors.toSet;

public class BlockingJenkinsPlugin_ParallelStream implements JenkinsPlugin {

    private final BlockingJiraServer srv;

    public BlockingJenkinsPlugin_ParallelStream(BlockingJiraServer srv, Executor dedicatedPool) {
        this.srv = srv;
    }

    public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return srv.findBundlesByName(bundleName)
            .stream().parallel()
            .flatMap(b -> srv.findComponentsByBundle(b).stream())
            .collect(toSet());
    }

}
