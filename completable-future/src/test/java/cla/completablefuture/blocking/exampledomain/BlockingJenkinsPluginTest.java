package cla.completablefuture.blocking.exampledomain;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import cla.completablefuture.exampledomain.*;
import com.jasongoodwin.monads.Try;
import org.junit.FixMethodOrder;
import org.junit.Test;
import static cla.completablefuture.blocking.exampledomain.FakeBlockingJiraServer.NB_OF_BUNDLES_PER_NAME;
import static cla.completablefuture.blocking.exampledomain.FakeBlockingJiraServer.NB_OF_COMPONENTS_PER_BUNDLE;
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
public class BlockingJenkinsPluginTest {

    @Test
    public void should_1_report_bundles_errors() {
        BlockingJiraServer jiraServer = mock(BlockingJiraServer.class);
        when(jiraServer.findBundlesByName(any())).thenThrow(new JiraServerException());
        JenkinsPlugin sut = new BlockingJenkinsPlugin_GenericCollect(jiraServer, newCachedThreadPool());
        
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
        BlockingJiraServer jiraServer = mock(BlockingJiraServer.class);
        JenkinsPlugin sut = new BlockingJenkinsPlugin_GenericCollect(jiraServer, newCachedThreadPool());
        when(jiraServer.findBundlesByName(any())).thenReturn(singleton(new JiraBundle()));
        when(jiraServer.findComponentsByBundle(any())).thenThrow(new JiraServerException());
        
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraServerException.class);
            }
        }
    }
    
    @Test public void should_3_be_fast() {
        List<BiFunction<BlockingJiraServer, Executor, JenkinsPlugin>> plugins = Arrays.asList(
            BlockingJenkinsPlugin_SequentialStream::new,
            BlockingJenkinsPlugin_ParallelStream::new,
            BlockingJenkinsPlugin_Reduce::new,
            BlockingJenkinsPlugin_Collect::new,
            BlockingJenkinsPlugin_GenericCollect::new,
            BlockingJenkinsPlugin_FactorCollect::new
        );
       
        BlockingJiraServer srv = new BlockingJiraServerWithLatency(new FakeBlockingJiraServer());
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
        JenkinsPlugin sut = new BlockingJenkinsPlugin_FactorCollect(
                new BlockingJiraServerWithLatency(new FakeBlockingJiraServer()),
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
        AsyncJenkinsPlugin sut = new BlockingJenkinsPlugin_GenericCollect(
            new BlockingJiraServerWithLatency(new FakeBlockingJiraServer()),
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
        JenkinsPlugin sut = new BlockingJenkinsPlugin_GenericCollect(
            new BlockingJiraServerWithLatency(new FakeBlockingJiraServer()),
            newCachedThreadPool()
        );
     
        assertThat(
            sut.findComponentsByBundleName("toto59")
        ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
    }
}
