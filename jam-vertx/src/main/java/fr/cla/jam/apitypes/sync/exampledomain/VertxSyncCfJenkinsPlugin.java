package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.CollectionSyncCfAdapter;
import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.apitypes.sync.VertxSyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.concurrent.Executor;

public class VertxSyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public VertxSyncCfJenkinsPlugin(SyncJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            SyncCfAdapter.adapt(srv::findBundlesByName, VertxSyncCfAdapter.supplyVertx()),
            bundles -> CollectionSyncCfAdapter.flatMapAdapt(
                bundles,
                srv::findComponentsByBundle,
                VertxSyncCfAdapter.supplyVertx(),
                Collections::emptySet,
                Sets::union
            )
        );
    }

}
