package fr.cla.jam.exampledomain.apitypes.completionstage;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.Csf;
import fr.cla.jam.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

class QuasarCsCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final CsJiraApi srv;
    private final FiberScheduler dedicatedScheduler;

    public QuasarCsCfJenkinsPlugin(CsJiraApi srv, FiberScheduler dedicatedScheduler) {
        super(srv);
        this.srv = srv;
        this.dedicatedScheduler = dedicatedScheduler;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofCs(bundleName, srv::findBundlesByName, dedicatedScheduler)
            .flatMapCs(srv::findComponentsByBundle, dedicatedScheduler);
    }

}
