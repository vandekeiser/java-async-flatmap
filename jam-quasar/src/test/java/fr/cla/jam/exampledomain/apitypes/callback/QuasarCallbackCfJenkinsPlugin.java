package fr.cla.jam.exampledomain.apitypes.callback;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.CfOfSet;
import fr.cla.jam.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

class QuasarCallbackCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final CallbackJiraApi srv;
    private final FiberScheduler quasar;

    public QuasarCallbackCfJenkinsPlugin(CallbackJiraApi srv, FiberScheduler quasar) {
        super(srv);
        this.srv = srv;
        this.quasar = quasar;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofCallback(bundleName, srv::findBundlesByName, quasar)
            .flatMapCallback(srv::findComponentsByBundle, quasar);
    }

}
