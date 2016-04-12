package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.apitypes.sync.VertxSyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import io.vertx.core.Vertx;

import java.util.concurrent.Executor;

public class VertxSyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public VertxSyncCfJenkinsPlugin(SyncJiraApi srv, Vertx vertx) {
        super(
            srv,
            VertxSyncCfAdapter.adapt(srv::findBundlesByName, vertx),
            bundles -> SyncCfAdapter.flatMapAdapt(
                bundles,
                srv::findComponentsByBundle,
                VertxSyncCfAdapter.supplyVertx()
            )
        );
    }

}
