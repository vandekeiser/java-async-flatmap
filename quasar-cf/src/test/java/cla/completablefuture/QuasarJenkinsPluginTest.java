package cla.completablefuture;

import cla.completablefuture.jenkins.*;
import cla.completablefuture.jira.*;
import com.jasongoodwin.monads.Try;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static cla.completablefuture.jira.FakeJiraServer.NB_OF_BUNDLES_PER_NAME;
import static cla.completablefuture.jira.FakeJiraServer.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//Run with 
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
@FixMethodOrder(NAME_ASCENDING)
public class QuasarJenkinsPluginTest {

    @Test
    public void should_1_report_bundles_errors() {
        JiraServer jiraServer = mock(JiraServer.class);
        when(jiraServer.findBundlesByName(any())).thenThrow(new JiraServerException());
        JenkinsPlugin sut = new JenkinsPlugin_GenericCollect_Quasar(jiraServer, newCachedThreadPool());
        
        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause().getCause() instanceof JiraServerException).isTrue();       
            }
        }
    }
    
    @Test
    public void should_2_report_components_errors() {
        JiraServer jiraServer = mock(JiraServer.class);
        JenkinsPlugin sut = new JenkinsPlugin_GenericCollect_Quasar(jiraServer, newCachedThreadPool());
        when(jiraServer.findBundlesByName(any())).thenReturn(singleton(new JiraBundle()));
        when(jiraServer.findComponentsByBundle(any())).thenThrow(new JiraServerException());
        
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause().getCause() instanceof JiraServerException).isTrue();       
            }
        }
    }
    
    @Test public void should_3_be_fast() throws FileNotFoundException {
        List<BiFunction<JiraServer, Executor, JenkinsPlugin>> plugins = Arrays.asList(
            JenkinsPlugin_SequentialStream::new,
            JenkinsPlugin_ParallelStream::new,
            JenkinsPlugin_Reduce::new,
            JenkinsPlugin_Collect::new,
            JenkinsPlugin_GenericCollect::new,
            JenkinsPlugin_FactorCollect::new,
            JenkinsPlugin_GenericCollect_Quasar::new
        );
       
        JiraServer srv = new JiraServerWithLatency(new FakeJiraServer());
        //Executor pool = newCachedThreadPool();
        Executor pool = newFixedThreadPool(commonPool().getParallelism());
        
        try(PrintStream oout = new DoublePrintStream("comparaison-latences.txt")) {
            oout.printf("Cores: %d, FJP size: %d%n", getRuntime().availableProcessors(), commonPool().getParallelism());
            plugins.stream()
                .map(p -> p.apply(srv, pool))
                .forEach(p -> {
                    Instant before = Instant.now();
                    Set<JiraComponent> answer = p.findComponentsByBundleName("toto59");
                    oout.printf("%-70s took %s (found %d) %n", p, Duration.between(before, Instant.now()), answer.size());
                });
        }

        
    }
    
    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new JenkinsPlugin_GenericCollect_Quasar(
                new JiraServerWithLatency(new FakeJiraServer()),
                newCachedThreadPool()
        );
        
        IntStream.rangeClosed(1, 1).forEach(i -> {
            out.println("i: " + i);
            assertThat(
                    sut.findComponentsByBundleName("toto59")
            ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
        });
    }
    
    @Test public void should_5_be_chainable() {
        AsyncJenkinsPlugin sut = new JenkinsPlugin_GenericCollect_Quasar(
            new JiraServerWithLatency(new FakeJiraServer()),
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
    
    @Test public void should_6_work_with_other_collections() {
        JenkinsPlugin sut = new JenkinsPlugin_GenericCollect_Quasar(
            new JiraServerWithLatency(new FakeJiraServer()),
            newCachedThreadPool()
        );
     
        assertThat(
            sut.findComponentsByBundleName("toto59")
        ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
    }
    
}
