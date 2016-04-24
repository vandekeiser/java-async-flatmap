package fr.cla.jam.apitypes.sync.exampledomain;

import fr.cla.jam.Csf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

import java.util.concurrent.Executor;

public class SyncCfJenkinsPlugin2 extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final SyncJiraApi srv;
    private final Executor pool;

    private SyncCfJenkinsPlugin2(SyncJiraApi srv, Executor pool) {
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
