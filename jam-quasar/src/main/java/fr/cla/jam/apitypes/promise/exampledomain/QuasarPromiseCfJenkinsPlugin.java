package fr.cla.jam.apitypes.promise.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.promise.QuasarPromiseCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarPromiseCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    private QuasarPromiseCfJenkinsPlugin(PromiseJiraApi srv, QuasarPromiseCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static QuasarPromiseCfJenkinsPlugin usingScheduler(PromiseJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        return new QuasarPromiseCfJenkinsPlugin(srv, new QuasarPromiseCfAdapter(dedicatedScheduler));
    }

}
