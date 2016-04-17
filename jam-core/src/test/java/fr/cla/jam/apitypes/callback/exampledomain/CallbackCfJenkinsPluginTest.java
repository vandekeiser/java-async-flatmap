package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.exampledomain.AbstractJenkinsPluginTest;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
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
    protected CfJenkinsPlugin defectiveSut() {
        CallbackJiraApi jira = new DefectiveCallbackJiraApi();
        return CallbackCfJenkinsPlugin.using(jira);
    }

    @Override
    protected CfJenkinsPlugin halfDefectiveSut() {
        CallbackJiraApi jira = new HalfDefectiveCallbackJiraApi();
        return CallbackCfJenkinsPlugin.using(jira);
    }

    @Override
    protected CfJenkinsPlugin latentSut() {
        return CallbackCfJenkinsPlugin.using(
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
            CallbackCfJenkinsPlugin.using(callbackApi)
        );
    }

}
