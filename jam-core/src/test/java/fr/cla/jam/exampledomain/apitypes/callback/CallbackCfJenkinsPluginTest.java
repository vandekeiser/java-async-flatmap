package fr.cla.jam.exampledomain.apitypes.callback;

import fr.cla.jam.exampledomain.AbstractJenkinsPluginTest;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import org.junit.FixMethodOrder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

//deepneural4j

//TODO:
// -tester scalabilite avec JMH
// -factor nb de resultats
// -separer modules fwk et domain
@FixMethodOrder(NAME_ASCENDING)
public class CallbackCfJenkinsPluginTest extends AbstractJenkinsPluginTest {

    @Override
    protected CsfJenkinsPlugin defectiveSut() {
        CallbackJiraApi jira = new DefectiveCallbackJiraApi();
        return new CallbackCfJenkinsPlugin(jira);
    }

    @Override
    protected CsfJenkinsPlugin halfDefectiveSut() {
        CallbackJiraApi jira = new HalfDefectiveCallbackJiraApi();
        return new CallbackCfJenkinsPlugin(jira);
    }

    @Override
    protected CsfJenkinsPlugin latentSut() {
        return new CallbackCfJenkinsPlugin(
            new BlockingLatentCallbackJiraApi(new FakeCallbackJiraApi())
        );
    }

    @Override
    protected int scalabilityTestParallelism() {
        return 1_000;
    }

    @Override
    protected int scalabilityTestConcurrency() {
        return 10;
    }

    @Override
    protected List<JenkinsPlugin> allPlugins(ExecutorService measurementPool) {
        CallbackJiraApi callbackApi = new BlockingLatentCallbackJiraApi(new FakeCallbackJiraApi());

        return Arrays.asList(
            new CallbackCfJenkinsPlugin(callbackApi)
        );
    }

}
