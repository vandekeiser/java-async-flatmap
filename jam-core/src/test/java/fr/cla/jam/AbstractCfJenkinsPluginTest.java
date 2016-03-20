package fr.cla.jam;

import com.jasongoodwin.monads.Try;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import fr.cla.jam.exampledomain.JiraApiException;
import fr.cla.jam.exampledomain.JiraComponent;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static fr.cla.jam.FakeApi.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.FakeApi.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

//deepneural4j

//Run with
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
// -javaagent:"C:\Users\User\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false

//TODO:
// -tester scalabilite avec JMH
// -factor nb de resultats
// -separer modules fwk et domain
@FixMethodOrder(NAME_ASCENDING)
public abstract class AbstractCfJenkinsPluginTest extends MeasuringTest {

    protected abstract CfJenkinsPlugin defectiveSut();
    @Test
    public void should_1_report_bundles_errors() {
        CfJenkinsPlugin sut = defectiveSut();
        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                try {
                    assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
                } catch (AssertionError possible) {
                    //happens eg. for QuasarCollectSyncApiCfJenkinsPluginTest
                    assertThat(expected.getCause().getCause()).isInstanceOf(JiraApiException.class);
                }
            }
        }
    }

    protected abstract CfJenkinsPlugin halfDefectiveSut();
    @Test
    public void should_2_report_components_errors() {
        JenkinsPlugin sut = halfDefectiveSut();
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                try {
                    assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
                } catch (AssertionError possible) {
                    //happens eg. for QuasarCollectSyncApiCfJenkinsPluginTest
                    assertThat(expected.getCause().getCause()).isInstanceOf(JiraApiException.class);
                }
            }
        }
    }

    protected abstract List<Function<Executor,JenkinsPlugin>> allPluginsForLatencyMeasurement();
    private static final Executor latencyMeasurementPool = newCachedThreadPool();
    @Test public void should_3_be_fast() throws FileNotFoundException {
        try(PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            printEnv(oout, latencyMeasurementPool);
            allPluginsForLatencyMeasurement().stream().forEach(pluginBuilder -> {
                JenkinsPlugin plugin = pluginBuilder.apply(latencyMeasurementPool);
                Instant before = Instant.now();
                Set<JiraComponent> answers = plugin.findComponentsByBundleName("toto59");
                printResult(oout, plugin, before, answers);
            });
        }
    }

    protected abstract int scalabilityTestParallelism();
    protected abstract int scalabilityTestConcurrency();
    protected abstract List<Function<Executor,JenkinsPlugin>> allPluginsForScalabilityMeasurement();
    @Test public void should_3bis_scale() throws FileNotFoundException {
        int CONCURRENCY = scalabilityTestConcurrency(), PARALLELISM = scalabilityTestParallelism();
        Executor scalabilityMeasurementPool = newFixedThreadPool(PARALLELISM);
        Set<JiraComponent> blackHole = new HashSet<>();

        try(PrintStream oout = new ConsolePlusFile("comparaison-scalabilite.txt")) {
            printEnv(oout, scalabilityMeasurementPool, CONCURRENCY);
            allPluginsForScalabilityMeasurement().stream()
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
        Executor clientsPool = newCachedThreadPool();
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(nAtATime);
        Instant b = Instant.now();
        out.println("STARTING measuring " + p);

        IntStream.range(0, nAtATime).forEach(i -> {
            //out.println("BEFORE " + i);
            clientsPool.execute(() -> {
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

    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = latentSut();
        
        IntStream.rangeClosed(1, 1).forEach(i -> {
            out.println("i: " + i);
            assertThat(
                sut.findComponentsByBundleName("toto59")
            ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
        });
    }

    protected abstract CfJenkinsPlugin latentSut();
    @Test public void should_5_be_chainable() {
        CfJenkinsPlugin sut = latentSut();
     
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
    
}
