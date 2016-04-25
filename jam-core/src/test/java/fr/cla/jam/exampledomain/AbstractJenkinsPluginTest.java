package fr.cla.jam.exampledomain;

import com.jasongoodwin.monads.Try;
import fr.cla.jam.util.ConsolePlusFile;
import fr.cla.jam.util.MeasuringTest;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static fr.cla.jam.util.FakeApi.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.util.FakeApi.NB_OF_COMPONENTS_PER_BUNDLE;
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
public abstract class AbstractJenkinsPluginTest extends MeasuringTest {

    protected abstract CsfJenkinsPlugin defectiveSut();
    @Test
    public void should_1_report_bundles_errors() {
        CsfJenkinsPlugin sut = defectiveSut();
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

    protected abstract CsfJenkinsPlugin halfDefectiveSut();
    @Test
    public void should_2_report_components_errors() {
        CsfJenkinsPlugin sut = halfDefectiveSut();
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

    protected List<JenkinsPlugin> allPluginsForLatencyMeasurement() {
        return allPlugins(latencyMeasurementPool);
    }
    protected abstract List<JenkinsPlugin> allPlugins(ExecutorService measurementPool);

    @Test public void should_3_be_fast() throws FileNotFoundException {
        try(PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            printEnv(oout, latencyMeasurementPool);
            allPluginsForLatencyMeasurement().stream().forEach(plugin -> {
                Instant before = Instant.now();
                Set<JiraComponent> answers = plugin.findComponentsByBundleName("toto59");
                printResult(oout, plugin, before, answers);
            });
        }
    }

    protected abstract int scalabilityTestParallelism();
    protected abstract int scalabilityTestConcurrency();
    protected List<JenkinsPlugin> allPluginsForScalabilityMeasurement() {
        return allPlugins(scalabilityMeasurementPool);
    }

    @Test public void should_3bis_scale() throws FileNotFoundException {
        int CONCURRENCY = scalabilityTestConcurrency();

        try(PrintStream oout = new ConsolePlusFile("comparaison-scalabilite.txt")) {
            printEnv(oout, scalabilityMeasurementPool, CONCURRENCY);
            allPluginsForScalabilityMeasurement().stream()
                .forEach(p -> nAtATime(CONCURRENCY, oout, p, () -> {
                    Instant before = Instant.now();
                    Set<JiraComponent> answers = p.findComponentsByBundleName("toto59");

                    printResult(out, p, before, answers);
                }));
        }
    }

    private static void nAtATime(int nAtATime, PrintStream oout, JenkinsPlugin p, Runnable r) {
        Executor clientsPool = newCachedThreadPool();
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(nAtATime);
        out.println("STARTING measuring " + p);
        Set<Long> durations = new ConcurrentSkipListSet<>();
        System.gc();System.gc();System.gc();sleep(5000);

        Instant b = Instant.now();
        IntStream.range(0, nAtATime).forEach(i -> {
            out.println("BEFORE " + i);
            clientsPool.execute(() -> {
                await(startGate);
                long before = System.nanoTime();
                r.run();
                durations.add(System.nanoTime() - before);
                endGate.countDown();
                out.println("AFTER " + i);
            });
        });
        startGate.countDown();
        await(endGate);

        LongSummaryStatistics stats = durations.stream().mapToLong(Long::longValue).summaryStatistics();
        oout.printf("DONE measuring %-90s %-12s avg=%.0fms(%d->%d) %n", p + ":", Duration.between(b, Instant.now()), stats.getAverage()/1_000_000, stats.getMin()/1_000_000, stats.getMax()/1_000_000);
    }

    private static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

    protected abstract CsfJenkinsPlugin latentSut();
    @Test public void should_5_be_chainable() {
        CsfJenkinsPlugin sut = latentSut();
     
        Set<JiraComponent> componentsOrEmpty = sut
            .findComponentsByBundleNameAsync("toto59")
            .asCf()    
            .exceptionally(t -> emptySet())
            .join();
        
        Optional<Set<JiraComponent>> maybeComponents = sut
            .findComponentsByBundleNameAsync("toto59")
            .asCf()    
            .thenApply(Optional::of).exceptionally(t -> Optional.empty())
            .join();
        
        Try<Set<JiraComponent>> tryComponents = sut
            .findComponentsByBundleNameAsync("toto59")
            .asCf()    
            .thenApply(Try::successful).exceptionally(Try::failure)
            .join();

        try {
            Set<JiraComponent> componentOrTimeout = sut
                .findComponentsByBundleNameAsync("toto59")
                .asCf()    
                .get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        } 
    }

    protected boolean useRealServer() {
        return getRealServer() != null;
    }

    protected String getRealServer() {
        return System.getProperty("ec2instance");
    }

    protected ExecutorService latencyMeasurementPool, scalabilityMeasurementPool;
    @Before public void setupPools() {
        latencyMeasurementPool = newCachedThreadPool();
        scalabilityMeasurementPool = newFixedThreadPool(scalabilityTestParallelism());
    }
    @After public void tearDownPools() {
        latencyMeasurementPool.shutdownNow();
        scalabilityMeasurementPool.shutdownNow();
    }

}
