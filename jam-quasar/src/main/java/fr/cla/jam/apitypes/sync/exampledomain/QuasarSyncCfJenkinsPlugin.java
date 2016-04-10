package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.CollectionSyncCfAdapter;
import fr.cla.jam.apitypes.sync.QuasarSyncCfAdapter;
import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.concurrent.Executor;

public class QuasarSyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public QuasarSyncCfJenkinsPlugin(SyncJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            SyncCfAdapter.adapt(srv::findBundlesByName, QuasarSyncCfAdapter.adapt()),
            bundles -> CollectionSyncCfAdapter.flatMapAdapt(
                bundles,
                srv::findComponentsByBundle,
                QuasarSyncCfAdapter.adapt(),
                Collections::emptySet,
                Sets::union
            )
        );
    }

}
