package fr.cla.jam.apitypes.callback.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.callback.QuasarCallbackCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarCallbackCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    private QuasarCallbackCfJenkinsPlugin(CallbackJiraApi srv, QuasarCallbackCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static QuasarCallbackCfJenkinsPlugin usingScheduler(CallbackJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        return new QuasarCallbackCfJenkinsPlugin(srv, new QuasarCallbackCfAdapter(dedicatedScheduler));
    }

}
