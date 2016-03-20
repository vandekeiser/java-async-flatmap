package fr.cla.jam.nonblocking.promise;

import com.jasongoodwin.monads.Try;
import fr.cla.jam.ConsolePlusFile;
import fr.cla.jam.MeasuringTest;
import fr.cla.jam.blocking.exampledomain.SyncJiraApi;
import fr.cla.jam.blocking.exampledomain.LatentSyncJiraApi;
import fr.cla.jam.blocking.exampledomain.FakeSyncJiraApi;
import fr.cla.jam.exampledomain.*;
import fr.cla.jam.nonblocking.completionstage.CsJiraApi;
import fr.cla.jam.nonblocking.completionstage.exampledomain.FakeCsJiraApi;
import fr.cla.jam.nonblocking.completionstage.exampledomain.LatentCsJiraApi;
import fr.cla.jam.nonblocking.promise.exampledomain.PromiseJiraApi;
import org.assertj.core.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static fr.cla.jam.FakeApi.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.FakeApi.NB_OF_COMPONENTS_PER_BUNDLE;
import static fr.cla.jam.util.functions.Functions.curry;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class QuasarCollectPromiseApiIntoCfJenkinsPluginTest extends MeasuringTest {

    @Test
    public void should_1_report_bundles_errors() {
        PromiseJiraApi jira = mock(PromiseJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            (onSuccess, onFailure) -> onFailure.accept(new JiraApiException())
        );

        JenkinsPlugin sut = new QuasarCollectPromiseApiIntoCfJenkinsPlugin(jira, newCachedThreadPool());

        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if (expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
            }
        }
    }

    @Test
    public void should_2_report_components_errors() {
        PromiseJiraApi jira = mock(PromiseJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(
            (onSuccess, onFailure) -> onSuccess.accept(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenReturn(
            (onSuccess, onFailure) -> onFailure.accept(new JiraApiException())
        );

        JenkinsPlugin sut = new QuasarCollectPromiseApiIntoCfJenkinsPlugin(jira, newCachedThreadPool());

        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if (expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
            }
        }
    }

    //private static final Executor pool = newFixedThreadPool(1);
    private static final Executor pool = newCachedThreadPool();

    @Test
    public void should_3_be_fast() throws FileNotFoundException {
        List<Function<Executor, JenkinsPlugin>> allPlugins = allPlugins();
        try (PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            printEnv(oout, pool);
            allPlugins.stream().forEach(pluginBuilder -> {
                JenkinsPlugin plugin = pluginBuilder.apply(pool);
                Instant before = Instant.now();
                Set<JiraComponent> answers = plugin.findComponentsByBundleName("toto59");
                printResult(oout, plugin, before, answers);
            });
        }
    }

    private static final int CONCURRENCY = 10, PARALLELISM = 100;
    //private static final Executor scalabilityMeasurementPool = newCachedThreadPool();
    private static final Executor scalabilityMeasurementPool = newFixedThreadPool(PARALLELISM);

    @Test
    public void should_3bis_scale() throws FileNotFoundException {
        Set<JiraComponent> blackHole = new HashSet<>();
        try (PrintStream oout = new ConsolePlusFile("comparaison-scalabilite.txt")) {
            printEnv(oout, scalabilityMeasurementPool, CONCURRENCY);
            allPlugins().stream()
                .map(p -> p.apply(scalabilityMeasurementPool))
                .forEach(p -> nAtATime(CONCURRENCY, oout, p, () -> {
                    Instant before = Instant.now();
                    Set<JiraComponent> answers = p.findComponentsByBundleName("toto59");
                    //printResult(out, p, before, answers);
                    blackHole.addAll(answers);
                }));
        }
        System.out.println(blackHole);
    }

    private static void nAtATime(int nAtATime, PrintStream oout, JenkinsPlugin p, Runnable r) {
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(nAtATime);
        Instant b = Instant.now();
        out.println("STARTING measuring " + p);

        IntStream.range(0, nAtATime).forEach(i -> {
            //out.println("BEFORE " + i);
            scalabilityMeasurementPool.execute(() -> {
                await(startGate);
                r.run();
                endGate.countDown();
                //out.println("AFTER " + i);
            });
        });

        startGate.countDown();
        await(endGate);
        oout.printf("DONE measuring %-90s %-12s%n", p + ":", Duration.between(b, Instant.now()));
    }

    private static void await(CountDownLatch startGate) {
        try {
            startGate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new QuasarCollectPromiseApiIntoCfJenkinsPlugin(
            new LatentPromiseJiraApi(new FakePromiseJiraApi()),
            newCachedThreadPool()
        );

        IntStream.rangeClosed(1, 1).forEach(i -> {
            out.println("i: " + i);
            Assertions.assertThat(
                sut.findComponentsByBundleName("toto59")
            ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
        });
    }

    @Test
    public void should_5_be_chainable() {
        CfJenkinsPlugin sut = new QuasarCollectPromiseApiIntoCfJenkinsPlugin(
            new LatentPromiseJiraApi(new FakePromiseJiraApi()),
            newCachedThreadPool()
        );

        Set<JiraComponent> componentsOrEmpty = sut
            .findComponentsByBundleNameAsync("toto59")
            .exceptionally(t -> emptySet())
            .join();

        Optional<Set<JiraComponent>> maybeComponents = sut
            .findComponentsByBundleNameAsync("toto59")
            .thenApply(Optional::of).exceptionally(t -> Optional.empty())
            .join();

        Try<Set<JiraComponent>> tryComponents = sut
            .findComponentsByBundleNameAsync("toto59")
            .thenApply(Try::successful).exceptionally(Try::failure)
            .join();

        try {
            Set<JiraComponent> componentOrTimeout = sut
                .findComponentsByBundleNameAsync("toto59")
                .get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private List<Function<Executor, JenkinsPlugin>> allPlugins() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
//            SequentialStreamSyncApiJenkinsPlugin::new
//            , ParallelStreamSyncApiJenkinsPlugin::new
                //CollectSyncApiCfJenkinsPlugin::new
//            , QuasarCollectSyncApiCfJenkinsPlugin::new
        );
        List<BiFunction<CsJiraApi, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
//            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Reduce::new //TODO?
//            CollectCsApiCfJenkinsPlugin::new
//            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_GenericCollect::new //TODO?
//            ,QuasarCollectCsApiIntoCfJenkinsPlugin::new
        );
        List<BiFunction<PromiseJiraApi, Executor, JenkinsPlugin>> promiseNonBlockingPlugins = Arrays.asList(
                QuasarCollectPromiseApiIntoCfJenkinsPlugin::new
        );

        SyncJiraApi blockingSrv = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi nonBlockingSrv = new LatentCsJiraApi(new FakeCsJiraApi());
        PromiseJiraApi promiseNonBlockingSrv = new LatentPromiseJiraApi(new FakePromiseJiraApi());

        List<Function<Executor, JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(blockingPlugins.stream().map(curry(blockingSrv)).collect(toList()));
        allPlugins.addAll(nonBlockingPlugins.stream().map(curry(nonBlockingSrv)).collect(toList()));
        allPlugins.addAll(promiseNonBlockingPlugins.stream().map(curry(promiseNonBlockingSrv)).collect(toList()));
        return allPlugins;
    }

}


