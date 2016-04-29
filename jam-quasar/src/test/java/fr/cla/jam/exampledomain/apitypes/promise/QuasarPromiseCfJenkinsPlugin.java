package fr.cla.jam.exampledomain.apitypes.promise;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.CfOfSet;
import fr.cla.jam.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

class QuasarPromiseCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final PromiseJiraApi srv;
    private final FiberScheduler quasar;

    public QuasarPromiseCfJenkinsPlugin(PromiseJiraApi srv, FiberScheduler quasar) {
        super(srv);
        this.srv = srv;
        this.quasar = quasar;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofPromise(bundleName, srv::findBundlesByName, quasar)
            .flatMapPromise(srv::findComponentsByBundle, quasar);
    }

}
