package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.exampledomain.*;
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
public class SyncCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CfJenkinsPlugin defectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return new SyncCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new SyncCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return new SyncCfJenkinsPlugin(
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
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> syncPlugins = Arrays.asList(
            SeqStreamJenkinsPlugin::new,
            ParStreamJenkinsPlugin::new,
            SyncCfJenkinsPlugin::new
        );

        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());

        List<Function<Executor, JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(syncPlugins.stream().map(curry(syncApi)).collect(toList()));
        return allPlugins;
    }

    @Override
    protected List<Function<Executor, JenkinsPlugin>> allPluginsForScalabilityMeasurement() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> syncPlugins = Arrays.asList(
            SyncCfJenkinsPlugin::new
        );

        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());

        List<Function<Executor, JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(syncPlugins.stream().map(curry(syncApi)).collect(toList()));
        return allPlugins;
    }
}
