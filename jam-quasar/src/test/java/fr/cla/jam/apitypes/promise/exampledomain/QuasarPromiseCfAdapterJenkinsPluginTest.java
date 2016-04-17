package fr.cla.jam.apitypes.promise.exampledomain;

import fr.cla.jam.apitypes.AbstractQuasarJenkinsPluginTest;
import fr.cla.jam.apitypes.promise.NonBlockingLatentPromiseJiraApi;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import fr.cla.jam.exampledomain.JiraApiException;
import fr.cla.jam.exampledomain.JiraBundle;
import org.junit.FixMethodOrder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.singleton;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class QuasarPromiseCfAdapterJenkinsPluginTest extends AbstractQuasarJenkinsPluginTest {

    @Override
    protected CfJenkinsPlugin defectiveSut() {
        PromiseJiraApi jira = mock(PromiseJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            (onSuccess, onFailure) -> onFailure.accept(new JiraApiException())
        );
        return QuasarPromiseCfJenkinsPlugin.usingScheduler(jira, dedicatedScheduler(latencyMeasurementPool));
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        PromiseJiraApi jira = mock(PromiseJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            (onSuccess, onFailure) -> onSuccess.accept(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenReturn(
            (onSuccess, onFailure) -> onFailure.accept(new JiraApiException())
        );
        return QuasarPromiseCfJenkinsPlugin.usingScheduler(jira, dedicatedScheduler(latencyMeasurementPool));
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return QuasarPromiseCfJenkinsPlugin.usingScheduler(
            new NonBlockingLatentPromiseJiraApi(new FakePromiseJiraApi()),
            dedicatedScheduler(latencyMeasurementPool)
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 1;
    }

    @Override
    protected int scalabilityTestConcurrency() {
        return 1_000;
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        PromiseJiraApi promiseApi = new NonBlockingLatentPromiseJiraApi(new FakePromiseJiraApi());

        return Arrays.asList(
            QuasarPromiseCfJenkinsPlugin.usingScheduler(promiseApi, dedicatedScheduler(measurementPool))
        );
    }

}


