package fr.cla.jam.exampledomain.apitypes.completionstage;

import fr.cla.jam.exampledomain.*;
import fr.cla.jam.exampledomain.apitypes.sync.FakeSyncJiraApi;
import fr.cla.jam.exampledomain.apitypes.sync.LatentSyncJiraApi;
import fr.cla.jam.exampledomain.apitypes.sync.SyncCfJenkinsPlugin;
import fr.cla.jam.exampledomain.apitypes.sync.SyncJiraApi;
import org.junit.FixMethodOrder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.singleton;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class CsCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CsfJenkinsPlugin defectiveSut() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return new CsCfJenkinsPlugin(jira);
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            CompletableFuture.completedFuture(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new CsCfJenkinsPlugin(jira);
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new CsCfJenkinsPlugin(
            new LatentCsJiraApi(new FakeCsJiraApi())
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
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi csApi = new LatentCsJiraApi(new FakeCsJiraApi());

        return Arrays.asList(
            new SyncCfJenkinsPlugin(syncApi, measurementPool),
            new CsCfJenkinsPlugin(csApi)
        );
    }

}
