package fr.cla.jam.blocking.exampledomain;

import com.jasongoodwin.monads.Try;
import fr.cla.jam.ConsolePlusFile;
import fr.cla.jam.MeasuringTest;
import fr.cla.jam.exampledomain.*;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.failBecauseExceptionWasNotThrown;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@FixMethodOrder(NAME_ASCENDING)
public class VertxCollectSyncCollectionApiCfJenkinsPluginTest extends MeasuringTest {

    @Test
    public void should_1_report_bundles_errors() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        JenkinsPlugin sut = new VertxCollectSyncCollectionApiCfJenkinsPlugin(jira, newCachedThreadPool());
        
        try {
            sut.findComponentsByBundleName("foo");
            failBecauseExceptionWasNotThrown(JiraApiException.class);
        } catch (JiraApiException | CompletionException expected) {
            if(expected instanceof CompletionException) {
                assertThat(expected.getCause()).isInstanceOf(JiraApiException.class);
            }
        }
    }
    
    @Test
    public void should_2_report_components_errors() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        JenkinsPlugin sut = new VertxCollectSyncCollectionApiCfJenkinsPlugin(jira, newCachedThreadPool());
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
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
    
    @Test public void should_3_be_fast() throws FileNotFoundException {
        List<BiFunction<SyncJiraApi, Executor, JenkinsPlugin>> plugins = Arrays.asList(
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new,
            CollectSyncApiCfJenkinsPlugin::new
        );

        SyncJiraApi srv = new LatentSyncJiraApi(new FakeSyncJiraApi());
        //Executor pool = newCachedThreadPool();
        Executor pool = newFixedThreadPool(1);
        
        try(PrintStream oout = new ConsolePlusFile("comparaison-latences.txt")) {
            printEnv(oout, pool);
            plugins.stream()
                .map(p -> p.apply(srv, pool))
                .forEach(p -> {
                    Instant before = Instant.now();
                    Set<JiraComponent> answers = p.findComponentsByBundleName("toto59");
                    printResult(oout, p, before, answers);
                });    
        } 
    }
    
    @Test public void should_4_find_the_right_nunmber_of_jira_components() {
        JenkinsPlugin sut = new VertxCollectSyncCollectionApiCfJenkinsPlugin(
                new LatentSyncJiraApi(new FakeSyncJiraApi()),
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
        CfJenkinsPlugin sut = new VertxCollectSyncCollectionApiCfJenkinsPlugin(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
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
        JenkinsPlugin sut = new VertxCollectSyncCollectionApiCfJenkinsPlugin(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
            newCachedThreadPool()
        );
     
        assertThat(
            sut.findComponentsByBundleName("toto59")
        ).hasSize(NB_OF_BUNDLES_PER_NAME * NB_OF_COMPONENTS_PER_BUNDLE);
    }
    
}
