package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.apitypes.sync.exampledomain.FakeSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.LatentSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.SyncCfJenkinsPlugin2;
import fr.cla.jam.apitypes.sync.exampledomain.SyncJiraApi;
import fr.cla.jam.exampledomain.*;
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
        return new CsCfJenkinsPlugin2(jira);
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            CompletableFuture.completedFuture(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new CsCfJenkinsPlugin2(jira);
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new CsCfJenkinsPlugin2(
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
            new SyncCfJenkinsPlugin2(syncApi, measurementPool),
            new CsCfJenkinsPlugin2(csApi)
        );
    }

}
