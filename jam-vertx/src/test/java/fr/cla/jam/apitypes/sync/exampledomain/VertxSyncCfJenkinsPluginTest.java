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
    protected CfJenkinsPlugin defectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return VertxSyncCfJenkinsPlugin.using(jira, vertx);
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return VertxSyncCfJenkinsPlugin.using(jira, vertx);
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return VertxSyncCfJenkinsPlugin.using(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
            vertx
        );
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        SyncJiraApi api = new LatentSyncJiraApi(new FakeSyncJiraApi());

        return Arrays.asList(
            SyncCfJenkinsPlugin.using(api, measurementPool),
            VertxSyncCfJenkinsPlugin.using(api, vertx)
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
