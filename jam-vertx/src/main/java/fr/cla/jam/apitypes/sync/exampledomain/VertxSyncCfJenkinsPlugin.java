package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.apitypes.sync.VertxSyncCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import io.vertx.core.Vertx;

public class VertxSyncCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    private VertxSyncCfJenkinsPlugin(SyncJiraApi srv, VertxSyncCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static VertxSyncCfJenkinsPlugin using(SyncJiraApi srv, Vertx vertx) {
        return new VertxSyncCfJenkinsPlugin(srv, new VertxSyncCfAdapter(vertx));
    }

}
