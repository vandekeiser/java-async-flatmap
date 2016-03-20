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
        return 10000;
    }

    @Override
    protected List<Function<Executor, JenkinsPlugin>> allPluginsForLatencyMeasurement() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> syncPlugins = Arrays.asList(
        );
        List<BiFunction<CsJiraApi, Executor, JenkinsPlugin>> csPlugins = Arrays.asList(
        );
        List<BiFunction<CallbackJiraApi, Executor, JenkinsPlugin>> callbackPlugins = Arrays.asList(
//            CollectCallbackApiIntoCfJenkinsPlugin::new,
//            QuasarCollectCallbackApiIntoCfJenkinsPlugin::new

                CollectCallbackApiIntoCfJenkinsPlugin::new

//                QuasarCollectCallbackApiIntoCfJenkinsPlugin::new
        );

        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi csApi = new LatentCsJiraApi(new FakeCsJiraApi());
//        CallbackJiraApi blockingCallbackApi = new BlockingLatentCallbackJiraApi(new FakeCallbackJiraApi());
        CallbackJiraApi nonBlockingCallbackApi = new NonBlockingLatentCallbackJiraApi(new FakeCallbackJiraApi());

        List<Function<Executor,JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(syncPlugins.stream().map(curry(syncApi)).collect(toList()));
        allPlugins.addAll(csPlugins.stream().map(curry(csApi)).collect(toList()));
//        allPlugins.addAll(callbackPlugins.stream().map(curry(blockingCallbackApi)).collect(toList()));
        allPlugins.addAll(callbackPlugins.stream().map(curry(nonBlockingCallbackApi)).collect(toList()));
        return allPlugins;
    }

    /*
    * AVEC -javaagent
    *   blockingCallbackApi + CollectCallbackApiIntoCfJenkinsPlugin = jamais
    *   blockingCallbackApi + QuasarCollectCallbackApiIntoCfJenkinsPlugin = jamais
    *   nonBlockingCallbackApi + CollectCallbackApiIntoCfJenkinsPlugin = PT6.957S
    *   nonBlockingCallbackApi + QuasarCollectCallbackApiIntoCfJenkinsPlugin = PT10.04S
    *
    * SANS -javaagent
    *   blockingCallbackApi + CollectCallbackApiIntoCfJenkinsPlugin = jamais
    *   blockingCallbackApi + QuasarCollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *   nonBlockingCallbackApi + CollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , PT8.659S
    *   nonBlockingCallbackApi + QuasarCollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , PT6.268S
    *
    * -->la presence de l'agent ne change rien!!
    * -->l'utilisation de l'api blocking/nonblocking fait toute la difference
    *   -->mais ca devrait etre impossible puisque les Fiber.sleep devbraient devenir des Thread.sleep
    *
    * SANS -javaagent, remplace fiber.sleep par thread.sleep dans nonBlockingCallbackApi
    *   nonBlockingCallbackApi(thread.sleep) + CollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , jamais
    * donc c'est bien ca
    *
    * Par contre, il y avait un truc bizarre ds AbstractLatentCallbackJiraApi:
    *   protected static final Executor delayExecutor = Executors.newFixedThreadPool(1);
    * On change juste ca, tjrs avec un sleep ds le supposement nonblocking:
    *   protected static final Executor delayExecutor = Executors.newCachedThreadPool();
    *
    * Ca donne:
    *   nonBlockingCallbackApi(thread.sleep) + CollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" ,  PT7.197S
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" ,  PT14.858S
    *
    * C'est uniquement le ralentissement du sleep qui cause ca
    * On passe CONCURRENCY à 10_000 pour voir si l'api cesse d'etre capable d'etre nonblocking et que ca s'effondre qd on depasse le nb de threads possibles:
    *
    *Ca donne:
    *   nonBlockingCallbackApi(thread.sleep) + CollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , PT1M34.403S
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , PT3M57.857S
    *
    * Je repasse en fiber.sleep pour voir si ca arrange (le delay executor est tjrs newCached):
    *   nonBlockingCallbackApi(thread.sleep) + CollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , PT1M50.697S
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , PT2M9.132S
    *Ca change rien..
    *
    * Je remet l'agent:
    *   nonBlockingCallbackApi(thread.sleep) + CollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *   nonBlockingCallbackApi(thread.sleep) + QuasarCollectCallbackApiIntoCfJenkinsPlugin = "QUASAR WARNING" , jamais
    *
    * Je remet le delaypool a 1, en fait son but etait justement d'eviter ca:
    *   -avec l'agent c'est long, mais on voit defiler les after xxx a 13s, puis OutOfMemoryError: GC overhead limit exceeded
     *  -sans l'agent c'est long, mais on voit defiler les after xxx a 1mn, puis OutOfMemoryError: GC overhead limit exceeded
    * */
}
