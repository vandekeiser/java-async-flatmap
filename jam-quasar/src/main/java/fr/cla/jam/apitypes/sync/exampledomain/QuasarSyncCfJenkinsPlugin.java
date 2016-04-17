package fr.cla.jam.apitypes.sync.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.sync.QuasarSyncCfAdapter2;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarSyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    private QuasarSyncCfJenkinsPlugin(SyncJiraApi srv, QuasarSyncCfAdapter2 adapter) {
        super(
            srv,
            adapter.adaptUsingScheduler(srv::findBundlesByName),
            bundles -> adapter.flatMapAdaptUsingScheduler(bundles, srv::findComponentsByBundle)
        );
    }

    public static QuasarSyncCfJenkinsPlugin using(SyncJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        return new QuasarSyncCfJenkinsPlugin(srv, new QuasarSyncCfAdapter2(dedicatedScheduler));
    }

}
