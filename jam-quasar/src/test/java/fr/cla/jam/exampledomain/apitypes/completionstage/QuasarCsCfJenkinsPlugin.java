package fr.cla.jam.exampledomain.apitypes.completionstage;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.CfOfSet;
import fr.cla.jam.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

class QuasarCsCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final CsJiraApi srv;
    private final FiberScheduler quasar;

    public QuasarCsCfJenkinsPlugin(CsJiraApi srv, FiberScheduler quasar) {
        super(srv);
        this.srv = srv;
        this.quasar = quasar;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofCs(bundleName, srv::findBundlesByName, quasar)
            .flatMapCs(srv::findComponentsByBundle, quasar);
    }

}
