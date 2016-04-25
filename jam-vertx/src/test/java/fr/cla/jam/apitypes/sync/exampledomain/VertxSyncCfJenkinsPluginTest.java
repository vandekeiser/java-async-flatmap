package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.*;
import io.vertx.core.Vertx;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.singleton;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VertxSyncCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    private static final Vertx vertx =  Vertx.vertx();

    @Override
    protected CsfJenkinsPlugin defectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return new VertxSyncCfJenkinsPlugin(jira, vertx);
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new VertxSyncCfJenkinsPlugin(jira, vertx);
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new VertxSyncCfJenkinsPlugin(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
            vertx
        );
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        SyncJiraApi api = new LatentSyncJiraApi(new FakeSyncJiraApi());

        return Arrays.asList(
            new SyncCfJenkinsPlugin(api, measurementPool),
            new VertxSyncCfJenkinsPlugin(api, vertx)
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 10;
    }

    @Override
    protected int scalabilityTestConcurrency() {
        return 2;
    }

}
