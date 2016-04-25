package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.AbstractQuasarJenkinsPluginTest;
import fr.cla.jam.exampledomain.*;
import org.junit.FixMethodOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.singleton;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//Run with 
// -javaagent:"C:\Users\Claisse\.m2\repository\co\paralleluniverse\quasar-core\0.7.4\quasar-core-0.7.4-jdk8.jar" -Dco.paralleluniverse.fibers.verifyInstrumentation=false
@FixMethodOrder(NAME_ASCENDING)
public class QuasarSyncCfJenkinsPluginTest extends AbstractQuasarJenkinsPluginTest {

    @Override
    protected CsfJenkinsPlugin defectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenThrow(new JiraApiException());
        return new QuasarSyncCfJenkinsPlugin2(jira, dedicatedScheduler(latencyMeasurementPool));
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        SyncJiraApi jira = mock(SyncJiraApi.class);
        when(jira.findBundlesByName(any())).thenReturn(singleton(new JiraBundle("the bundle")));
        when(jira.findComponentsByBundle(any())).thenThrow(new JiraApiException());
        return new QuasarSyncCfJenkinsPlugin2(jira, dedicatedScheduler(latencyMeasurementPool));
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new QuasarSyncCfJenkinsPlugin2(
            new LatentSyncJiraApi(new FakeSyncJiraApi()),
            dedicatedScheduler(latencyMeasurementPool)
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 1;
    }

    @Override
    protected int scalabilityTestConcurrency() {
        return 10;
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        SyncJiraApi syncApi = new LatentSyncJiraApi(new FakeSyncJiraApi());
        List<JenkinsPlugin> all = new ArrayList<>();

        all.add(new SyncCfJenkinsPlugin2(syncApi, measurementPool));
        all.add(new QuasarSyncCfJenkinsPlugin2(syncApi, dedicatedScheduler(measurementPool)));

        if(useRealServer()) {
            SyncJiraApi realServerSyncApi = new RealServerLatencySyncApi(new FakeSyncJiraApi(), getRealServer());
            all.add(new SyncCfJenkinsPlugin2(realServerSyncApi, dedicatedScheduler(measurementPool)));
        }

        return all;
    }

}
