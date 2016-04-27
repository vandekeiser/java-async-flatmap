package fr.cla.jam.exampledomain.apitypes.promise;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.Csf;
import fr.cla.jam.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

class QuasarPromiseCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final PromiseJiraApi srv;
    private final FiberScheduler dedicatedScheduler;

    public QuasarPromiseCfJenkinsPlugin(PromiseJiraApi srv, FiberScheduler dedicatedScheduler) {
        super(srv);
        this.srv = srv;
        this.dedicatedScheduler = dedicatedScheduler;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofPromise(bundleName, srv::findBundlesByName, dedicatedScheduler)
            .flatMapPromise(srv::findComponentsByBundle, dedicatedScheduler);
    }

}