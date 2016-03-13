package cla.completablefuture.nonblocking;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import cla.completablefuture.jenkins.AsyncJenkinsPlugin;
import cla.completablefuture.jenkins.JenkinsPlugin;
import cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Collect_Quasar;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.JiraServerException;
import cla.completablefuture.jira.nonblocking.FakeJiraServer;
import cla.completablefuture.jira.nonblocking.JiraServer;
import cla.completablefuture.jira.nonblocking.JiraServerWithLatency;
import com.jasongoodwin.monads.Try;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import static cla.completablefuture.jira.nonblocking.FakeJiraServer.NB_OF_BUNDLES_PER_NAME;
import static cla.completablefuture.jira.nonblocking.FakeJiraServer.NB_OF_COMPONENTS_PER_BUNDLE;
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
public class QuasarJenkinsPluginTest {

    @Test
    public void should_1_report_bundles_errors() {
        JiraServer jiraServer = mock(JiraServer.class);
        when(jiraServer.findBundlesByName(any())).thenThrow(new JiraServerException());
        JenkinsPlugin sut = new JenkinsPlugin_Collect_Quasar(jiraServer, newCachedThreadPool());

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
        JenkinsPlugin sut = new JenkinsPlugin_Collect_Quasar(jiraServer, newCachedThreadPool());
        when(jiraServer.findBundlesByName(any())).thenReturn(
            completedFuture(singleton(new JiraBundle()))
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
        List<JenkinsPlugin> allPlugins = allPlugins();
        
        out.printf("Cores: %d, FJP size: %d%n", getRuntime().availableProcessors(), commonPool().getParallelism());
        allPlugins.stream().forEach(p -> {
            Instant before = Instant.now();
            Set<JiraComponent> answer = p.findComponentsByBundleName("toto59");
            out.printf("%-80s took %s (found %d) %n", p, Duration.between(before, Instant.now()), answer.size());
        });
    }

    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new JenkinsPlugin_Collect_Quasar(
            new JiraServerWithLatency(new FakeJiraServer()),
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
        AsyncJenkinsPlugin sut = new JenkinsPlugin_Collect_Quasar(
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
    
    private List<JenkinsPlugin> allPlugins() {
        List<BiFunction<cla.completablefuture.jira.blocking.JiraServer, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_SequentialStream::new,
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_ParallelStream::new,
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_Reduce::new,
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_Collect::new,
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_GenericCollect::new
        );
        List<BiFunction<JiraServer, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Reduce::new,
            cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Collect::new,
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_GenericCollect::new,
            cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Collect_Quasar::new
        );

        cla.completablefuture.jira.blocking.JiraServer blockingSrv = new cla.completablefuture.jira.blocking.JiraServerWithLatency(new cla.completablefuture.jira.blocking.FakeJiraServer());
        JiraServer nonBlockingSrv = new JiraServerWithLatency(new FakeJiraServer());
        //Executor pool = newCachedThreadPool();
        Executor pool = newFixedThreadPool(1);

        List<JenkinsPlugin> allPlugins = new ArrayList<>();
        allPlugins.addAll(
            blockingPlugins.stream().map(p -> p.apply(blockingSrv, pool)
        ).collect(toList()));
        allPlugins.addAll(
            nonBlockingPlugins.stream().map(p -> p.apply(nonBlockingSrv, pool)
        ).collect(toList()));
        return allPlugins;
    }

}
