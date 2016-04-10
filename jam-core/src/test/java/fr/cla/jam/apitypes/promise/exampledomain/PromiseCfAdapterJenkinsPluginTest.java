package fr.cla.jam.apitypes.promise.exampledomain;

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
public class PromiseCfAdapterJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CfJenkinsPlugin defectiveSut() {
        PromiseJiraApi jira = mock(PromiseJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            (onSuccess, onFailure) -> onFailure.accept(new JiraApiException())
        );
        return new PromiseCfJenkinsPlugin(jira, newCachedThreadPool());
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
        return new PromiseCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return new PromiseCfJenkinsPlugin(
            new BlockingLatentPromiseJiraApi(new FakePromiseJiraApi()),
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
        PromiseJiraApi promiseApi = new BlockingLatentPromiseJiraApi(new FakePromiseJiraApi());

        return Arrays.asList(
            new PromiseCfJenkinsPlugin(promiseApi, measurementPool)
        );
    }

}


