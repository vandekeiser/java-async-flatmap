package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.SetSyncCfAdapter;
import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class SyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public SyncCfJenkinsPlugin(SyncJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            SyncCfAdapter.adaptUsingPool(srv::findBundlesByName, dedicatedPool),
            bundles -> SetSyncCfAdapter.flatMapAdaptUsingPool(bundles, srv::findComponentsByBundle, dedicatedPool)
        );
    }

}
