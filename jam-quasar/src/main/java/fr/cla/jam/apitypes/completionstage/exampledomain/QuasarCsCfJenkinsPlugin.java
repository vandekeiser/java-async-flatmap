package fr.cla.jam.apitypes.completionstage.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.apitypes.completionstage.QuasarCsCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;

import java.util.Set;

public class QuasarCsCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public QuasarCsCfJenkinsPlugin(CsJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        super(
            srv,
            QuasarCsCfAdapter.<String, Set<JiraBundle>>adapt(dedicatedScheduler).apply(srv::findBundlesByName),
            bundles -> CsCfAdapter.flatMapAdapt(
                bundles,
                srv::findComponentsByBundle,
                QuasarCsCfAdapter.adapt(dedicatedScheduler)
            )
        );
    }

}
