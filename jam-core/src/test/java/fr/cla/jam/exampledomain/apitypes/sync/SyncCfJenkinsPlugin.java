package fr.cla.jam.exampledomain.apitypes.sync;

import fr.cla.jam.Csf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.concurrent.Executor;

public class SyncCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final SyncJiraApi srv;
    private final Executor pool;

    public SyncCfJenkinsPlugin(SyncJiraApi srv, Executor pool) {
        super(srv);
        this.srv = srv;
        this.pool = pool;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return Csf
            .ofSync(bundleName, srv::findBundlesByName, pool)
            .flatMapSync(srv::findComponentsByBundle, pool);
    }

}