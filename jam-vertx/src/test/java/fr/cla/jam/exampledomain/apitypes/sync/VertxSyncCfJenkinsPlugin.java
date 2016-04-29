package fr.cla.jam.exampledomain.apitypes.sync;

import fr.cla.jam.CfOfSet;
import fr.cla.jam.VertxCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;
import io.vertx.core.Vertx;

class VertxSyncCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final SyncJiraApi srv;
    private final Vertx vertx;

    public VertxSyncCfJenkinsPlugin(SyncJiraApi srv, Vertx vertx) {
        super(srv);
        this.srv = srv;
        this.vertx = vertx;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return VertxCsf
            .ofSync(bundleName, srv::findBundlesByName, vertx)
            .flatMapSync(srv::findComponentsByBundle, vertx);
    }
}
