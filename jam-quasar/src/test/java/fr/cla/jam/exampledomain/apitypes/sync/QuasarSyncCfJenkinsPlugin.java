package fr.cla.jam.exampledomain.apitypes.sync;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.CfOfSet;
import fr.cla.jam.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

class QuasarSyncCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final SyncJiraApi srv;
    private final FiberScheduler quasar;

    public QuasarSyncCfJenkinsPlugin(SyncJiraApi srv, FiberScheduler quasar) {
        super(srv);
        this.srv = srv;
        this.quasar = quasar;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofSync(bundleName, srv::findBundlesByName, quasar)
            .flatMapSync(srv::findComponentsByBundle, quasar);
    }

}
