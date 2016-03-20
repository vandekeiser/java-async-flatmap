package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.completionstage.exampledomain.CsJiraApi;
import fr.cla.jam.apitypes.completionstage.exampledomain.FakeCsJiraApi;
import fr.cla.jam.apitypes.completionstage.exampledomain.LatentCsJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.FakeSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.LatentSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.SyncJiraApi;
import fr.cla.jam.exampledomain.AbstractJenkinsPluginTest;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import org.junit.FixMethodOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

import static fr.cla.jam.util.functions.Functions.curry;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

//deepneural4j

//TODO:
// -tester scalabilite avec JMH
// -factor nb de resultats
// -separer modules fwk et domain
@FixMethodOrder(NAME_ASCENDING)
public class CollectCallbackApiIntoCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CfJenkinsPlugin defectiveSut() {
        CallbackJiraApi jira = new DefectiveCallbackJiraApi();
        return new CollectCallbackApiIntoCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        CallbackJiraApi jira = new HalfDefectiveCallbackJiraApi();
        return new CollectCallbackApiIntoCfJenkinsPlugin(jira, newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return new CollectCallbackApiIntoCfJenkinsPlugin(
            new BlockingLatentCallbackJiraApi(new FakeCallbackJiraApi()),
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
        );
        List<BiFunction<CsJiraApi, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
        );
        List<BiFunction<CallbackJiraApi, Executor, JenkinsPlugin>> callbackNonBlockingPlugins = Arrays.asList(
            CollectCallbackApiIntoCfJenkinsPlugin::new
        );

        SyncJiraApi blockingSrv = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi nonBlockingSrv = new LatentCsJiraApi(new FakeCsJiraApi());
        CallbackJiraApi callbackNonBlockingSrv = new BlockingLatentCallbackJiraApi(new FakeCallbackJiraApi());

        List<Function<Executor,JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(blockingPlugins.stream().map(curry(blockingSrv)).collect(toList()));
        allPlugins.addAll(nonBlockingPlugins.stream().map(curry(nonBlockingSrv)).collect(toList()));
        allPlugins.addAll(callbackNonBlockingPlugins.stream().map(curry(callbackNonBlockingSrv)).collect(toList()));
        return allPlugins;
    }

}
