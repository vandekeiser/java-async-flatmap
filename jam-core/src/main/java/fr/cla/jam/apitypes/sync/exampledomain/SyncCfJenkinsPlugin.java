package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.PoolSetSyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class SyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    private SyncCfJenkinsPlugin(SyncJiraApi srv, PoolSetSyncCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static SyncCfJenkinsPlugin using(SyncJiraApi srv, Executor dedicatedPool) {
        return new SyncCfJenkinsPlugin(srv, new PoolSetSyncCfAdapter(dedicatedPool));
    }

}
