package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.AbstractJenkinsPluginTest;
import fr.cla.jam.apitypes.completionstage.exampledomain.CsJiraApi;
import fr.cla.jam.apitypes.completionstage.exampledomain.FakeCsJiraApi;
import fr.cla.jam.apitypes.completionstage.exampledomain.LatentCsJiraApi;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import fr.cla.jam.exampledomain.JiraApiException;
import fr.cla.jam.exampledomain.JiraBundle;
import org.junit.FixMethodOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

import static fr.cla.jam.util.functions.Functions.curry;
import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class CollectSyncApiCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CfJenkinsPlugin defectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return new CollectSyncApiCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new CollectSyncApiCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return new CollectSyncApiCfJenkinsPlugin(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
            newCachedThreadPool()
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 1000;
    }

    @Override
    protected int scalabilityTestConcurrency() {
        return 10;
    }

    @Override
    protected List<Function<Executor, JenkinsPlugin>> allPluginsForLatencyMeasurement() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
            SequentialStreamSyncApiJenkinsPlugin::new,
            ParallelStreamSyncApiJenkinsPlugin::new,
            ReduceSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new
        );
        List<BiFunction<CsJiraApi, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
        );

        SyncJiraApi blockingSrv = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi nonBlockingSrv = new LatentCsJiraApi(new FakeCsJiraApi());

        List<Function<Executor, JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(blockingPlugins.stream().map(curry(blockingSrv)).collect(toList()));
        allPlugins.addAll(nonBlockingPlugins.stream().map(curry(nonBlockingSrv)).collect(toList()));
        return allPlugins;
    }

    @Override
    protected List<Function<Executor, JenkinsPlugin>> allPluginsForScalabilityMeasurement() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
            CollectSyncApiCfJenkinsPlugin::new
        );
        List<BiFunction<CsJiraApi, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
        );

        SyncJiraApi blockingSrv = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi nonBlockingSrv = new LatentCsJiraApi(new FakeCsJiraApi());

        List<Function<Executor, JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(blockingPlugins.stream().map(curry(blockingSrv)).collect(toList()));
        allPlugins.addAll(nonBlockingPlugins.stream().map(curry(nonBlockingSrv)).collect(toList()));
        return allPlugins;
    }
}
