package fr.cla.jam.apitypes.completionstage.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.completionstage.QuasarCsCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarCsCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    public QuasarCsCfJenkinsPlugin(CsJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        super(
            srv,
            QuasarCsCfAdapter.adapt(srv::findBundlesByName, dedicatedScheduler),
            bundles -> QuasarCsCfAdapter.flatMapAdapt(bundles, srv::findComponentsByBundle, dedicatedScheduler)
        );
    }

}
