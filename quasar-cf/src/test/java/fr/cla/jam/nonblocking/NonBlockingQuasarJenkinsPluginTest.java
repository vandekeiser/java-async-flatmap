package fr.cla.jam.nonblocking;

import fr.cla.jam.ConsolePlusFile;
import fr.cla.jam.MeasuringTest;
import fr.cla.jam.blocking.exampledomain.*;
import fr.cla.jam.exampledomain.*;
import fr.cla.jam.nonblocking.completionstage.NonBlockingJiraServer;
import fr.cla.jam.nonblocking.completionstage.exampledomain.NonBlockingJenkinsPlugin_Collect;
import fr.cla.jam.nonblocking.completionstage.exampledomain.NonBlockingJenkinsPlugin_Collect_Quasar;
import fr.cla.jam.nonblocking.exampledomain.FakeNonBlockingJiraServer;
import fr.cla.jam.nonblocking.exampledomain.NonBlockingJiraServerWithLatency;
import com.jasongoodwin.monads.Try;
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

import static fr.cla.jam.nonblocking.exampledomain.FakeNonBlockingJiraServer.NB_OF_BUNDLES_PER_NAME;
import static fr.cla.jam.nonblocking.exampledomain.FakeNonBlockingJiraServer.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.ForkJoinPool.commonPool;
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
        NonBlockingJiraServer jiraServer = mock(NonBlockingJiraServer.class);
        //attention probablement pas correct!!! plutot then return completed (failure),
        // ce qui expliquerait la necessite du catch degueu en plus du onfail
        when(jiraServer.findBundlesByName(any())).thenReturn(failure());
        JenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(jiraServer, newCachedThreadPool());

        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraServerException.class);
            }
        }
    }
    
    @Test
    public void should_2_report_components_errors() {
        NonBlockingJiraServer jiraServer = mock(NonBlockingJiraServer.class);
        JenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(jiraServer, newCachedThreadPool());
        when(jiraServer.findBundlesByName(any())).thenReturn(
            completedFuture(singleton(new JiraBundle()))
        );
        when(jiraServer.findComponentsByBundle(any())).thenReturn(failure());
        
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraServerException.class);
            }
        }
    }

    //Executor pool = newCachedThreadPool();
    Executor pool = newFixedThreadPool(1);
    @Test public void should_3_be_fast() throws FileNotFoundException {
        List<JenkinsPlugin> allPlugins = allPlugins();

        try(PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            printEnv(oout, pool);
            allPlugins.stream().forEach(p -> {
                Instant before = Instant.now();
                Set<JiraComponent> answers = p.findComponentsByBundleName("toto59");
                printResult(oout, p, before, answers);
            });
        }
    }

    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(
            new NonBlockingJiraServerWithLatency(new FakeNonBlockingJiraServer()),
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
        AsyncJenkinsPlugin sut = new NonBlockingJenkinsPlugin_Collect_Quasar(
            new NonBlockingJiraServerWithLatency(new FakeNonBlockingJiraServer()),
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
        List<BiFunction<BlockingJiraServer, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
            BlockingJenkinsPlugin_SequentialStream::new,
            BlockingJenkinsPlugin_ParallelStream::new,
            BlockingJenkinsPlugin_Collect::new,
            BlockingJenkinsPlugin_GenericCollect_Quasar::new
        );
        List<BiFunction<NonBlockingJiraServer, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
                //TODO?
                //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Reduce::new,
                NonBlockingJenkinsPlugin_Collect::new,
                //TODO?
                //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_GenericCollect::new,
                NonBlockingJenkinsPlugin_Collect_Quasar::new
        );

        BlockingJiraServer blockingSrv = new BlockingJiraServerWithLatency(new FakeBlockingJiraServer());
        NonBlockingJiraServer nonBlockingSrv = new NonBlockingJiraServerWithLatency(new FakeNonBlockingJiraServer());

        List<JenkinsPlugin> allPlugins = new ArrayList<>();
        allPlugins.addAll(
            blockingPlugins.stream().map(p -> p.apply(blockingSrv, pool)
        ).collect(toList()));
        allPlugins.addAll(
            nonBlockingPlugins.stream().map(p -> p.apply(nonBlockingSrv, pool)
        ).collect(toList()));
        return allPlugins;
    }

    private static <T> CompletionStage<T> failure() {
        CompletableFuture<T> failed = new CompletableFuture<>();
        failed.completeExceptionally(new JiraServerException());
        return failed;
    }

}
