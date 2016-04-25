package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.*;
import org.junit.FixMethodOrder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class SyncCfJenkinsPluginTest extends AbstractJenkinsPluginTest2 {

    @Override
    protected CsfJenkinsPlugin defectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return new SyncCfJenkinsPlugin2(jira, newCachedThreadPool());
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new SyncCfJenkinsPlugin2(jira, newCachedThreadPool());
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new SyncCfJenkinsPlugin2(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
            newCachedThreadPool()
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 1_000;
    }

    @Override
    protected int scalabilityTestConcurrency() {
        return 10;
    }

    @Override
    protected List<JenkinsPlugin> allPluginsForLatencyMeasurement() {
        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());

        return Arrays.asList(
            new SeqStreamJenkinsPlugin(syncApi),
            new ParStreamJenkinsPlugin(syncApi),
            SyncCfJenkinsPlugin.using(syncApi, latencyMeasurementPool)
        );
    }

    @Override
    protected List<JenkinsPlugin> allPluginsForScalabilityMeasurement() {
        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());

        return Arrays.asList(
            SyncCfJenkinsPlugin.using(syncApi, scalabilityMeasurementPool)
        );
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        throw new UnsupportedOperationException("different plugins for latency and scalability measurement here");
    }

}
