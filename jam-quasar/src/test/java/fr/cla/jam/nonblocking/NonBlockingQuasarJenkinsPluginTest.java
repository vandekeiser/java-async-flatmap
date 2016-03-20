package fr.cla.jam.nonblocking;

import com.jasongoodwin.monads.Try;
import fr.cla.jam.ConsolePlusFile;
import fr.cla.jam.MeasuringTest;
import fr.cla.jam.blocking.exampledomain.SyncJiraApi;
import fr.cla.jam.blocking.exampledomain.SyncJiraApiWithLatency;
import fr.cla.jam.blocking.exampledomain.FakeSyncJiraApi;
import fr.cla.jam.exampledomain.*;
import fr.cla.jam.nonblocking.completionstage.CompletionStageJiraApi;
import fr.cla.jam.nonblocking.completionstage.exampledomain.NonBlockingJenkinsPlugin_Collect;
import fr.cla.jam.nonblocking.completionstage.exampledomain.NonBlockingJenkinsPlugin_Collect_Quasar;
import fr.cla.jam.nonblocking.exampledomain.FakeCompletionStageJiraApi;
import fr.cla.jam.nonblocking.exampledomain.CompletionStageJiraApiWithLatency;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static fr.cla.jam.FakeApi.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.FakeApi.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//Run with
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
// -javaagent:"C:\Users\User\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
@Ignore
@FixMethodOrder(NAME_ASCENDING)
public class NonBlockingQuasarJenkinsPluginTest extends MeasuringTest {

    @Test
    public void should_1_report_bundles_errors() {
        CompletionStageJiraApi jira = mock(CompletionStageJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(failure());
        JenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(jira, newCachedThreadPool());

        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
            }
        }
    }
    
    @Test
    public void should_2_report_components_errors() {
        CompletionStageJiraApi jira = mock(CompletionStageJiraApi.class);
        JenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(jira, newCachedThreadPool());
        when(jira.findBundlesByName(any())).thenReturn(
            completedFuture(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenReturn(failure());
        
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
            }
        }
    }

    private static final Executor latencyMeasurementPool = newFixedThreadPool(1);
    @Test public void should_3_be_fast() throws FileNotFoundException {
        List<JenkinsPlugin> allPlugins = allPlugins();

        try(PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            printEnv(oout, latencyMeasurementPool);
            allPlugins.stream().forEach(p -> {
                Instant before = Instant.now();
                Set<JiraComponent> answers = p.findComponentsByBundleName("toto59");
                printResult(oout, p, before, answers);
            });
        }
    }

    private static final int CONCURRENCY = 10;
    private static final Executor scalabilityMeasurementPool = newCachedThreadPool();
    @Test public void should_3bis_scale() throws FileNotFoundException {
        try(PrintStream oout = new ConsolePlusFile("comparaison-scalabilite.txt")) {
            printEnv(oout, scalabilityMeasurementPool, CONCURRENCY);
            allPlugins().stream().forEach(p -> nAtATime(CONCURRENCY, oout, p, () -> {
                Instant before = Instant.now();
                Set<JiraComponent> answers = p.findComponentsByBundleName("toto59");
                printResult(out, p, before, answers);
            }));
        }
    }

    private static void nAtATime(int nAtATime, PrintStream oout, JenkinsPlugin p, Runnable r) {
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(nAtATime);
        Instant b = Instant.now();
        out.println("STARTING measuring " + p);

        IntStream.range(0, nAtATime).forEach(i -> {
            out.println("BEFORE " + i);
            scalabilityMeasurementPool.execute(() -> {
                await(startGate);
                r.run();
                endGate.countDown();
                out.println("AFTER " + i);
            });
        });

        startGate.countDown();
        await(endGate);
        oout.printf("DONE measuring %-90s %-12s%n", p + ":", Duration.between(b, Instant.now()));
    }

    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(
            new CompletionStageJiraApiWithLatency(new FakeCompletionStageJiraApi()),
            newCachedThreadPool()
        );
        
        IntStream.rangeClosed(1, 10).forEach(i -> {
            out.println("i: " + i);
            assertThat(
                sut.findComponentsByBundleName("toto59")
            ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
        });
    }
    
    @Test public void should_5_be_chainable() {
        CfJenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(
            new CompletionStageJiraApiWithLatency(new FakeCompletionStageJiraApi()),
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
    
    private List<JenkinsPlugin> allPlugins() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
//            SequentialStreamSyncApiJenkinsPlugin::new,
//            ParallelStreamSyncApiJenkinsPlugin::new,
            //CollectSyncApiCfJenkinsPlugin::new
//            BlockingJenkinsPlugin_GenericCollect_Quasar::new
        );
        List<BiFunction<CompletionStageJiraApi, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Reduce::new,
            NonBlockingJenkinsPlugin_Collect::new
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_GenericCollect::new,
            , NonBlockingJenkinsPlugin_Collect_Quasar::new
        );

        SyncJiraApi blockingSrv = new SyncJiraApiWithLatency(new FakeSyncJiraApi());
        CompletionStageJiraApi nonBlockingSrv = new CompletionStageJiraApiWithLatency(new FakeCompletionStageJiraApi());

        List<JenkinsPlugin> allPlugins = new ArrayList<>();
        allPlugins.addAll(
            blockingPlugins.stream().map(p -> p.apply(blockingSrv, latencyMeasurementPool)
        ).collect(toList()));
        allPlugins.addAll(
            nonBlockingPlugins.stream().map(p -> p.apply(nonBlockingSrv, latencyMeasurementPool)
        ).collect(toList()));
        return allPlugins;
    }

    private static <T> CompletionStage<T> failure() {
        CompletableFuture<T> failed = new CompletableFuture<>();
        failed.completeExceptionally(new JiraApiException());
        return failed;
    }

    private static void await(CountDownLatch startGate) {
        try {
            startGate.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
