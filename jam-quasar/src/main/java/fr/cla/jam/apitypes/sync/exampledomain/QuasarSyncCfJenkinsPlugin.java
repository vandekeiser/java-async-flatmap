package fr.cla.jam.apitypes.sync.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.sync.QuasarSyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarSyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    private QuasarSyncCfJenkinsPlugin(SyncJiraApi srv, QuasarSyncCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static QuasarSyncCfJenkinsPlugin using(SyncJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        return new QuasarSyncCfJenkinsPlugin(srv, new QuasarSyncCfAdapter(dedicatedScheduler));
    }

}
