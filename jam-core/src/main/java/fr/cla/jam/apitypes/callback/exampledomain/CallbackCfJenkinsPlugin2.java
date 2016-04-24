package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.Csf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

public class CallbackCfJenkinsPlugin2 extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final CallbackJiraApi srv;

    public CallbackCfJenkinsPlugin2(CallbackJiraApi srv) {
        super(srv);
        this.srv = srv;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return Csf
            .ofCallback(bundleName, srv::findBundlesByName)
            .flatMapCallback(srv::findComponentsByBundle);
    }

}
