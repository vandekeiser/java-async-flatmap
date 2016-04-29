package fr.cla.jam.exampledomain.apitypes.completionstage;

import fr.cla.jam.exampledomain.*;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//Run with
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
// -javaagent:"C:\Users\User\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
@Ignore
public class QuasarCsCfAdapterJenkinsPluginTest extends AbstractQuasarJenkinsPluginTest {

    @Override
    protected CsfJenkinsPlugin defectiveSut() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(failure());
        return new QuasarCsCfJenkinsPlugin(jira, toQuasar(latencyMeasurementPool));
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            completedFuture(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenReturn(failure());
        return new QuasarCsCfJenkinsPlugin(jira, toQuasar(latencyMeasurementPool));
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new QuasarCsCfJenkinsPlugin(
            new LatentCsJiraApi(new FakeCsJiraApi()),
            toQuasar(latencyMeasurementPool)
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
            new CsCfJenkinsPlugin(csApi),
            new QuasarCsCfJenkinsPlugin(csApi, toQuasar(measurementPool))
        );
    }

    private static <T> CompletionStage<T> failure() {
        CompletableFuture<T> failed = new CompletableFuture<>();
        failed.completeExceptionally(new JiraApiException());
        return failed;
    }

}
