package fr.cla.jam.apitypes.sync.exampledomain;

import com.jasongoodwin.monads.Try;
import fr.cla.jam.apitypes.callback.exampledomain.CallbackCfJenkinsPlugin;
import fr.cla.jam.apitypes.callback.exampledomain.CallbackJiraApi;
import fr.cla.jam.apitypes.callback.exampledomain.FakeCallbackJiraApi;
import fr.cla.jam.exampledomain.*;
import fr.cla.jam.util.ConsolePlusFile;
import fr.cla.jam.util.MeasuringTest;
import io.vertx.core.Vertx;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static fr.cla.jam.util.FakeApi.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.util.FakeApi.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VertxSyncCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    private static final Vertx vertx =  Vertx.vertx();


    @Override
    protected CfJenkinsPlugin defectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return new VertxSyncCfJenkinsPlugin(jira, vertx);
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new VertxSyncCfJenkinsPlugin(jira, vertx);
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return new VertxSyncCfJenkinsPlugin(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
            vertx
        );
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        SyncJiraApi api = new LatentSyncJiraApi(new FakeSyncJiraApi());

        return Arrays.asList(
            SyncCfJenkinsPlugin.using(api, measurementPool),
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
