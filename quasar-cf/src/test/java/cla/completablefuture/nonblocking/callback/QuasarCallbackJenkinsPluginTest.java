package cla.completablefuture.nonblocking.callback;

import cla.completablefuture.ConsolePlusFile;
import cla.completablefuture.Functions;
import cla.completablefuture.jenkins.AsyncJenkinsPlugin;
import cla.completablefuture.jenkins.JenkinsPlugin;
import cla.completablefuture.jenkins.nonblocking.callback.JenkinsPlugin_CallbackCollect_Quasar;
import cla.completablefuture.jira.JiraBundle;
import cla.completablefuture.jira.JiraComponent;
import cla.completablefuture.jira.JiraServerException;
import cla.completablefuture.jira.nonblocking.FakeJiraServer;
import cla.completablefuture.jira.nonblocking.JiraServer;
import cla.completablefuture.jira.nonblocking.JiraServerWithLatency;
import cla.completablefuture.jira.nonblocking.callback.CallbackJiraServer;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static cla.completablefuture.Functions.curry;
import static cla.completablefuture.jira.nonblocking.FakeJiraServer.NB_OF_BUNDLES_PER_NAME;
import static cla.completablefuture.jira.nonblocking.FakeJiraServer.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
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

//TODO:
// -tester scalabilite JMH
// -latence: prendre un nouveau pool pour chaque plugin? pqoi ca gene de reutiliser le mm??
// -pqoi la version callback est-elle bcp plus lente que si l'api est CF??
@Ignore
@FixMethodOrder(NAME_ASCENDING)
public class QuasarCallbackJenkinsPluginTest {
    
    @Test
    public void should_1_report_bundles_errors() {
        CallbackJiraServer jiraServer = mock(CallbackJiraServer.class);
        when(jiraServer.findBundlesByName(any())).thenReturn(
            (onSuccess, onFailure) -> onFailure.accept(new JiraServerException())      
        );
        JenkinsPlugin sut = new JenkinsPlugin_CallbackCollect_Quasar(jiraServer, newCachedThreadPool());

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
        CallbackJiraServer jiraServer = mock(CallbackJiraServer.class);
        JenkinsPlugin sut = new JenkinsPlugin_CallbackCollect_Quasar(jiraServer, newCachedThreadPool());
        when(jiraServer.findBundlesByName(any())).thenReturn(
            (onSuccess, onFailure) -> onSuccess.accept(singleton(new JiraBundle()))
        );
        when(jiraServer.findComponentsByBundle(any())).thenReturn(
            (onSuccess, onFailure) -> onFailure.accept(new JiraServerException())
        );
        
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraServerException.class);
        } catch (JiraServerException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraServerException.class);
            }
        }
    }

    private static final Supplier<ExecutorService> poolSupplier = () -> newFixedThreadPool(1);
    @Test public void should_3_be_fast() throws FileNotFoundException {
        List<Function<Executor,JenkinsPlugin>> allPlugins = allPlugins();

        try(PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            oout.printf("Cores: %d, FJP size: %d%n", getRuntime().availableProcessors(), commonPool().getParallelism());
            allPlugins.stream().forEach(pluginBuilder -> {
                ExecutorService pool = poolSupplier.get();
                try {
                    JenkinsPlugin plugin = pluginBuilder.apply(pool);
                    Instant before = Instant.now();
                    Set<JiraComponent> answer = plugin.findComponentsByBundleName("toto59");
                    oout.printf("%-80s took %s (found %d) %n", plugin, Duration.between(before, Instant.now()), answer.size());
                } finally {
                    pool.shutdownNow();
                }
            });
        }
    }

    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new JenkinsPlugin_CallbackCollect_Quasar(
            new CallbackJiraServerWithLatency(new FakeCallbackJiraServer()),
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
        AsyncJenkinsPlugin sut = new JenkinsPlugin_CallbackCollect_Quasar(
            new CallbackJiraServerWithLatency(new FakeCallbackJiraServer()),
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
    
    private List<Function<Executor,JenkinsPlugin>> allPlugins() {
        List<BiFunction<cla.completablefuture.jira.blocking.JiraServer, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_SequentialStream::new,
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_ParallelStream::new,
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_Collect::new,
            cla.completablefuture.jenkins.blocking.JenkinsPlugin_GenericCollect_Quasar::new
        );
        List<BiFunction<JiraServer, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Reduce::new,
            cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Collect::new,
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_GenericCollect::new,
            cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Collect_Quasar::new
        );
        List<BiFunction<CallbackJiraServer, Executor, JenkinsPlugin>> callbackNonBlockingPlugins = Arrays.asList(
                cla.completablefuture.jenkins.nonblocking.callback.JenkinsPlugin_CallbackCollect_Quasar::new
        );

        cla.completablefuture.jira.blocking.JiraServer blockingSrv = new cla.completablefuture.jira.blocking.JiraServerWithLatency(new cla.completablefuture.jira.blocking.FakeJiraServer());
        JiraServer nonBlockingSrv = new JiraServerWithLatency(new FakeJiraServer());
        CallbackJiraServer callbackNonBlockingSrv = new CallbackJiraServerWithLatency(new FakeCallbackJiraServer());

        List<Function<Executor,JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(blockingPlugins.stream().map(curry(blockingSrv)).collect(toList()));
        allPlugins.addAll(nonBlockingPlugins.stream().map(curry(nonBlockingSrv)).collect(toList()));
        allPlugins.addAll(callbackNonBlockingPlugins.stream().map(curry(callbackNonBlockingSrv)).collect(toList()));
        return allPlugins;
    }

}
