package fr.cla.jam.apitypes.sync.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.sync.QuasarSyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarSyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public QuasarSyncCfJenkinsPlugin(SyncJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        super(
            srv,
            QuasarSyncCfAdapter.adaptUsingScheduler(srv::findBundlesByName, dedicatedScheduler),
            bundles -> QuasarSyncCfAdapter.flatMapAdaptUsingScheduler(bundles, srv::findComponentsByBundle, dedicatedScheduler)
        );
    }

}
