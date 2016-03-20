package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.NonBlockingLatentCallbackJiraApi;
import fr.cla.jam.apitypes.completionstage.exampledomain.CsJiraApi;
import fr.cla.jam.apitypes.completionstage.exampledomain.FakeCsJiraApi;
import fr.cla.jam.apitypes.completionstage.exampledomain.LatentCsJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.FakeSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.LatentSyncJiraApi;
import fr.cla.jam.apitypes.sync.exampledomain.SyncJiraApi;
import fr.cla.jam.exampledomain.AbstractJenkinsPluginTest;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

import static fr.cla.jam.util.functions.Functions.curry;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;

//deepneural4j

//Run with
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
// -javaagent:"C:\Users\User\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false

//TODO:
// -tester scalabilite avec JMH
// -factor nb de resultats
// -separer modules fwk et domain
public class QuasarCollectCallbackApiIntoCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CfJenkinsPlugin defectiveSut() {
        return new QuasarCollectCallbackApiIntoCfJenkinsPlugin(new DefectiveCallbackJiraApi(), newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        return new QuasarCollectCallbackApiIntoCfJenkinsPlugin(new HalfDefectiveCallbackJiraApi(), newCachedThreadPool());
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return new QuasarCollectCallbackApiIntoCfJenkinsPlugin(
            new NonBlockingLatentCallbackJiraApi(new FakeCallbackJiraApi()),
            newCachedThreadPool()
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 1;
    }

    @Override
    protected int scalabilityTestConcurrency() {
        return 1000;
    }

    @Override
    protected List<Function<Executor, JenkinsPlugin>> allPluginsForLatencyMeasurement() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> syncPlugins = Arrays.asList(
        );
        List<BiFunction<CsJiraApi, Executor, JenkinsPlugin>> csPlugins = Arrays.asList(
        );
        List<BiFunction<CallbackJiraApi, Executor, JenkinsPlugin>> callbackPlugins = Arrays.asList(
            CollectCallbackApiIntoCfJenkinsPlugin::new,
            QuasarCollectCallbackApiIntoCfJenkinsPlugin::new
        );

        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi csApi = new LatentCsJiraApi(new FakeCsJiraApi());
        CallbackJiraApi callbackApi = new NonBlockingLatentCallbackJiraApi(new FakeCallbackJiraApi());

        List<Function<Executor,JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(syncPlugins.stream().map(curry(syncApi)).collect(toList()));
        allPlugins.addAll(csPlugins.stream().map(curry(csApi)).collect(toList()));
        allPlugins.addAll(callbackPlugins.stream().map(curry(callbackApi)).collect(toList()));
        return allPlugins;
    }

}
