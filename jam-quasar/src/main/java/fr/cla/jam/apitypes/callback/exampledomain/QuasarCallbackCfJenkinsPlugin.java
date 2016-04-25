package fr.cla.jam.apitypes.callback.exampledomain;

import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.Csf;
import fr.cla.jam.apitypes.QuasarCsf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

public class QuasarCallbackCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final CallbackJiraApi srv;
    private final FiberScheduler dedicatedScheduler;

    public QuasarCallbackCfJenkinsPlugin(CallbackJiraApi srv, FiberScheduler dedicatedScheduler) {
        super(srv);
        this.srv = srv;
        this.dedicatedScheduler = dedicatedScheduler;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return QuasarCsf
            .ofCallback(bundleName, srv::findBundlesByName, dedicatedScheduler)
            .flatMapCallback(srv::findComponentsByBundle, dedicatedScheduler);
    }

}
