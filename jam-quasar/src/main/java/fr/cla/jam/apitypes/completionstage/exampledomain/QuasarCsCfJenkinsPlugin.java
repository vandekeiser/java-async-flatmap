package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.apitypes.completionstage.QuasarCsCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;

import java.util.Set;
import java.util.concurrent.Executor;

public class QuasarCsCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public QuasarCsCfJenkinsPlugin(CsJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            QuasarCsCfAdapter.<String, Set<JiraBundle>>usingPool(dedicatedPool).apply(srv::findBundlesByName),
            bundles -> CsCfAdapter.flatMapAdapt(
                bundles,
                srv::findComponentsByBundle,
                QuasarCsCfAdapter.usingPool(dedicatedPool)
            )
        );
    }

}
