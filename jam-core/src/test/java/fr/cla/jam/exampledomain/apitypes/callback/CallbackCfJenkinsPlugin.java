package fr.cla.jam.exampledomain.apitypes.callback;

import fr.cla.jam.CfOfSet;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

public class CallbackCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final CallbackJiraApi srv;

    public CallbackCfJenkinsPlugin(CallbackJiraApi srv) {
        super(srv);
        this.srv = srv;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return CfOfSet
            .ofCallback(bundleName, srv::findBundlesByName)
            .flatMapCallback(srv::findComponentsByBundle);
    }

}
