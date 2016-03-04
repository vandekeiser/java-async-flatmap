package cla.completablefuture.nonblocking;

import cla.completablefuture.jenkins.AsyncJenkinsPlugin;
import cla.completablefuture.jenkins.JenkinsPlugin;
import cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Collect;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.JiraServerException;
import cla.completablefuture.jira.nonblocking.FakeJiraServer;
import cla.completablefuture.jira.nonblocking.JiraServer;
import cla.completablefuture.jira.nonblocking.JiraServerWithLatency;
import com.jasongoodwin.monads.Try;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class JenkinsPluginTest {

    @Test
    public void should_1_report_bundles_errors() {
        JiraServer jiraServer = mock(JiraServer.class);
        when(jiraServer.findBundlesByName(any())).thenThrow(new JiraServerException());
        JenkinsPlugin sut = new JenkinsPlugin_Collect(jiraServer, newCachedThreadPool());
        
        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause() instanceof JiraServerException).isTrue();       
            }
        }
    }
    
    @Test
    public void should_2_report_components_errors() {
        JiraServer jiraServer = mock(JiraServer.class);
        JenkinsPlugin sut = new JenkinsPlugin_Collect(jiraServer, newCachedThreadPool());
        when(jiraServer.findBundlesByName(any())).thenReturn(
                CompletableFuture.completedFuture(singleton(new JiraBundle()))
        );
        when(jiraServer.findComponentsByBundle(any())).thenThrow(new JiraServerException());
        
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause() instanceof JiraServerException).isTrue();       
            }
        }
    }
    
    @Test public void should_3_be_fast() {
        List<BiFunction<JiraServer, Executor, JenkinsPlugin>> plugins = Arrays.asList(
            JenkinsPlugin_Collect::new
        );
       
        JiraServer srv = new JiraServerWithLatency(new FakeJiraServer());
        Executor pool = newCachedThreadPool();
        //Executor pool = newFixedThreadPool(10);
        
        out.printf("Cores: %d, FJP size: %d%n", getRuntime().availableProcessors(), commonPool().getParallelism());
        plugins.stream()
            .map(p -> p.apply(srv, pool))
            .forEach(p -> {
                Instant before = Instant.now();
                Set<JiraComponent> answer = p.findComponentsByBundleName("toto59");
                out.printf("%-80s took %s (found %d) %n", p, Duration.between(before, Instant.now()), answer.size());
            });
    }
    
    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new JenkinsPlugin_Collect(
                new JiraServerWithLatency(new FakeJiraServer()),
                newCachedThreadPool()
        );
        
        IntStream.rangeClosed(1, 1).forEach(i -> {
            out.println("i: " + i);
            assertThat(
                    sut.findComponentsByBundleName("toto59")
            ).hasSize(FakeJiraServer.NB_OF_BUNDLES_PER_NAME * FakeJiraServer.NB_OF_COMPONENTS_PER_BUNDLE);
        });
    }
    
    @Test public void should_5_be_chainable() {
        AsyncJenkinsPlugin sut = new JenkinsPlugin_Collect(
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
        JenkinsPlugin sut = new JenkinsPlugin_Collect(
            new JiraServerWithLatency(new FakeJiraServer()),
            newCachedThreadPool()
        );
     
        assertThat(
            sut.findComponentsByBundleName("toto59")
        ).hasSize(FakeJiraServer.NB_OF_BUNDLES_PER_NAME * FakeJiraServer.NB_OF_COMPONENTS_PER_BUNDLE);
    }
}
