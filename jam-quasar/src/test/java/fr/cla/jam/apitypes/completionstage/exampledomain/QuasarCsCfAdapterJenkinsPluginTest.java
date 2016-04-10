package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.apitypes.sync.exampledomain.FakeSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.LatentSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.SyncCfJenkinsPlugin;
import fr.cla.jam.apitypes.sync.exampledomain.SyncJiraApi;
import fr.cla.jam.exampledomain.*;
import org.junit.FixMethodOrder;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;

import static fr.cla.jam.util.functions.Functions.curry;
import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//Run with
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
// -javaagent:"C:\Users\User\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
@Ignore
@FixMethodOrder(NAME_ASCENDING)
public class QuasarCsCfAdapterJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CfJenkinsPlugin defectiveSut() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(failure());
        return new QuasarCsCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            completedFuture(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenReturn(failure());
        return new QuasarCsCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return new QuasarCsCfJenkinsPlugin(
            new LatentCsJiraApi(new FakeCsJiraApi()),
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
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        CsJiraApi csApi = new LatentCsJiraApi(new FakeCsJiraApi());

        return Arrays.asList(
            new CsCfJenkinsPlugin(csApi, measurementPool),
            new QuasarCsCfJenkinsPlugin(csApi, measurementPool)
        );
    }

    private static <T> CompletionStage<T> failure() {
        CompletableFuture<T> failed = new CompletableFuture<>();
        failed.completeExceptionally(new JiraApiException());
        return failed;
    }

}
