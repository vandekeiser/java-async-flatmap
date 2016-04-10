package fr.cla.jam.apitypes.promise.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.promise.QuasarPromiseCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarPromiseCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    public QuasarPromiseCfJenkinsPlugin(PromiseJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        super(
            srv,
            QuasarPromiseCfAdapter.adapt(srv::findBundlesByName, dedicatedScheduler),
            bundles -> QuasarPromiseCfAdapter.adaptFlatMap(bundles, srv::findComponentsByBundle, dedicatedScheduler)
        );
    }

}
