package fr.cla.jam.apitypes.sync.exampledomain;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.Csf;
import fr.cla.jam.apitypes.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

public class QuasarSyncCfJenkinsPlugin2 extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final SyncJiraApi srv;
    private final FiberScheduler dedicatedScheduler;

    private QuasarSyncCfJenkinsPlugin2(SyncJiraApi srv, FiberScheduler dedicatedScheduler) {
        super(srv);
        this.srv = srv;
        this.dedicatedScheduler = dedicatedScheduler;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofSync(bundleName, srv::findBundlesByName, dedicatedScheduler)
            .flatMapSync(srv::findComponentsByBundle, dedicatedScheduler);
    }

}
