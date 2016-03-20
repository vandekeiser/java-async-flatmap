package fr.cla.jam.nonblocking.completionstage.exampledomain;

import com.jasongoodwin.monads.Try;
import fr.cla.jam.MeasuringTest;
import fr.cla.jam.blocking.exampledomain.CollectSyncApiCfJenkinsPlugin;
import fr.cla.jam.blocking.exampledomain.FakeSyncJiraApi;
import fr.cla.jam.blocking.exampledomain.LatentSyncJiraApi;
import fr.cla.jam.blocking.exampledomain.SyncJiraApi;
import fr.cla.jam.exampledomain.*;
import org.junit.FixMethodOrder;
import org.junit.Test;

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
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class CollectCsApiCfJenkinsPluginTest extends MeasuringTest {

    @Test
    public void should_1_report_bundles_errors() {
        CsJiraApi jira = mock(CsJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        JenkinsPlugin sut = new CollectCsApiCfJenkinsPlugin(jira, newCachedThreadPool());
        
        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause() instanceof JiraApiException).isTrue();
            }
        }
    }
    
    @Test
    public void should_2_report_components_errors() {
        CsJiraApi jira = mock(CsJiraApi.class);
        JenkinsPlugin sut = new CollectCsApiCfJenkinsPlugin(jira, newCachedThreadPool());
        when(jira.findBundlesByName(any())).thenReturn(
                CompletableFuture.completedFuture(singleton(new JiraBundle("the bundle")))
        );
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        
        try {
            sut.findComponentsByBundleName("foo");    
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
            }
        }
    }

    //Executor pool = newCachedThreadPool();
    Executor pool = newFixedThreadPool(1);
    @Test public void should_3_be_fast() {
        List<? extends JenkinsPlugin> allPlugins = allPlugins();

        printEnv(out, pool);
        allPlugins.stream()
            .forEach(p -> {
                Instant before = Instant.now();
                Set<JiraComponent> answers = p.findComponentsByBundleName("toto59");
                printResult(out, p, before, answers);
            });
    }

    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new CollectCsApiCfJenkinsPlugin(
                new LatentCsJiraApi(new FakeCsJiraApi()),
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
        CfJenkinsPlugin sut = new CollectCsApiCfJenkinsPlugin(
            new LatentCsJiraApi(new FakeCsJiraApi()),
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
        JenkinsPlugin sut = new CollectCsApiCfJenkinsPlugin(
            new LatentCsJiraApi(new FakeCsJiraApi()),
            newCachedThreadPool()
        );
     
        assertThat(
            sut.findComponentsByBundleName("toto59")
        ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
    }

    private List<? extends JenkinsPlugin> allPlugins() {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> blockingPlugins = Arrays.asList(
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new
        );
        List<BiFunction<CsJiraApi, Executor, JenkinsPlugin>> nonBlockingPlugins = Arrays.asList(
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_Reduce::new,
            CollectCsApiCfJenkinsPlugin::new
            //TODO?
            //cla.completablefuture.jenkins.nonblocking.JenkinsPlugin_GenericCollect::new,
        );

        SyncJiraApi blockingSrv = new LatentSyncJiraApi(new FakeSyncJiraApi());
        CsJiraApi nonBlockingSrv = new LatentCsJiraApi(new FakeCsJiraApi());

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
