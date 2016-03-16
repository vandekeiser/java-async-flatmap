package cla.completablefuture.jenkins.blocking;

import java.util.Set;
import java.util.concurrent.Executor;
import cla.completablefuture.jenkins.JenkinsPlugin;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.blocking.BlockingJiraServer;
import static java.util.stream.Collectors.toSet;

public class BlockingJenkinsPlugin_SequentialStream implements JenkinsPlugin {

    private final BlockingJiraServer srv;

    public BlockingJenkinsPlugin_SequentialStream(BlockingJiraServer srv, Executor dedicatedPool) {
        this.srv = srv;
    }

    public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return srv.findBundlesByName(bundleName)
            .stream()
            .flatMap(b -> srv.findComponentsByBundle(b).stream())
            .collect(toSet());
    }

}
