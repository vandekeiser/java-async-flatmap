package cla.completablefuture.jenkins;

import cla.completablefuture.jira.JiraServer;
import cla.completablefuture.jira.JiraComponent;

import java.util.Set;
import java.util.concurrent.Executor;

import static java.util.stream.Collectors.toSet;

public class JenkinsPlugin_SequentialStream implements JenkinsPlugin {

    private final JiraServer srv;

    public JenkinsPlugin_SequentialStream(JiraServer srv, Executor dedicatedPool) {
        this.srv = srv;
    }

    public Set<JiraComponent> findComponentsByBundleName(String bundleName) {
        return srv.findBundlesByName(bundleName)
            .stream()
            .flatMap(b -> srv.findComponentsByBundle(b).stream())
            .collect(toSet());
    }

}
