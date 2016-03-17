package cla.completablefuture.nonblocking.callback;

import cla.completablefuture.ConsolePlusFile;
import cla.completablefuture.blocking.exampledomain.*;
import cla.completablefuture.exampledomain.*;
import cla.completablefuture.nonblocking.callback.exampledomain.CallbackJiraServer;
import cla.completablefuture.nonblocking.callback.exampledomain.JenkinsPlugin_CallbackCollect_Quasar;
import cla.completablefuture.nonblocking.completionstage.NonBlockingJiraServer;
import cla.completablefuture.nonblocking.completionstage.exampledomain.NonBlockingJenkinsPlugin_Collect;
import cla.completablefuture.nonblocking.completionstage.exampledomain.NonBlockingJenkinsPlugin_Collect_Quasar;
import cla.completablefuture.nonblocking.exampledomain.FakeNonBlockingJiraServer;
import cla.completablefuture.nonblocking.exampledomain.NonBlockingJiraServerWithLatency;
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
import java.util.stream.IntStream;

import static cla.completablefuture.Functions.curry;
import static cla.completablefuture.nonblocking.exampledomain.FakeNonBlockingJiraServer.NB_OF_BUNDLES_PER_NAME;
import static cla.completablefuture.nonblocking.exampledomain.FakeNonBlockingJiraServer.NB_OF_COMPONENTS_PER_BUNDLE;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.concurrent.Executors.newCachedThreadPool;
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

    //private static final Executor pool = newFixedThreadPool(1);
    private static final Executor pool = newCachedThreadPool();
    @Test public void should_3_be_fast() throws FileNotFoundException {
        List<Function<Executor,JenkinsPlugin>> allPlugins = allPlugins();

        try(PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            oout.printf("Cores: %d, FJP size: %d%n", getRuntime().availableProcessors(), commonPool().getParallelism());
            allPlugins.stream().forEach(pluginBuilder -> {
                JenkinsPlugin plugin = pluginBuilder.apply(pool);
                Instant before = Instant.now();
                Set<JiraComponent> answer = plugin.findComponentsByBundleName("toto59");
                oout.printf("%-70s took %s (found %d) %n", pretty(plugin), Duration.between(before, Instant.now()), answer.size());
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
        List<BiFunction<CallbackJiraServer, Executor, JenkinsPlugin>> callbackNonBlockingPlugins = Arrays.asList(
                JenkinsPlugin_CallbackCollect_Quasar::new
        );

        BlockingJiraServer blockingSrv = new BlockingJiraServerWithLatency(new FakeBlockingJiraServer());
        NonBlockingJiraServer nonBlockingSrv = new NonBlockingJiraServerWithLatency(new FakeNonBlockingJiraServer());
        CallbackJiraServer callbackNonBlockingSrv = new CallbackJiraServerWithLatency(new FakeCallbackJiraServer());

        List<Function<Executor,JenkinsPlugin>> allPlugins = new ArrayList<>();
        allPlugins.addAll(blockingPlugins.stream().map(curry(blockingSrv)).collect(toList()));
        allPlugins.addAll(nonBlockingPlugins.stream().map(curry(nonBlockingSrv)).collect(toList()));
        allPlugins.addAll(callbackNonBlockingPlugins.stream().map(curry(callbackNonBlockingSrv)).collect(toList()));
        return allPlugins;
    }

    private static String pretty(JenkinsPlugin plugin) {
        return plugin.getClass().getName().replace("cla.completablefuture.jenkins.", "");
    }
}
