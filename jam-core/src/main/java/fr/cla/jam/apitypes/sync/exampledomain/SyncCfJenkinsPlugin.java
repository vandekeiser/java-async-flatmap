package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.SetSyncCfAdapter;
import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class SyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    private SyncCfJenkinsPlugin(SyncJiraApi srv, SetSyncCfAdapter adapter) {
        super(
            srv,
            adapter.adaptUsingPool(srv::findBundlesByName),
            bundles -> adapter.flatMapAdaptUsingPool(bundles, srv::findComponentsByBundle)
        );
    }

    public static SyncCfJenkinsPlugin using(SyncJiraApi srv, Executor dedicatedPool) {
        return new SyncCfJenkinsPlugin(srv, new SetSyncCfAdapter(dedicatedPool));
    }

}
