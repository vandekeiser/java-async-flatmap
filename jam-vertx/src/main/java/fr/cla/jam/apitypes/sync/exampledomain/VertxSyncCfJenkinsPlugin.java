package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.Csf;
import fr.cla.jam.apitypes.VertxCsf;
import fr.cla.jam.apitypes.sync.VertxSyncCfAdapter;
import fr.cla.jam.exampledomain.*;
import io.vertx.core.Vertx;

public class VertxSyncCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final SyncJiraApi srv;
    private final Vertx vertx;

    public VertxSyncCfJenkinsPlugin(SyncJiraApi srv, Vertx vertx) {
        super(srv);
        this.srv = srv;
        this.vertx = vertx;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return VertxCsf
            .ofSync(bundleName, srv::findBundlesByName, vertx)
            .flatMapSync(srv::findComponentsByBundle, vertx);
    }
}
