package fr.cla.jam.exampledomain.apitypes.promise;

import fr.cla.jam.CfOfSet;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

public class PromiseCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final PromiseJiraApi srv;

    public PromiseCfJenkinsPlugin(PromiseJiraApi srv) {
        super(srv);
        this.srv = srv;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return CfOfSet
            .ofPromise(bundleName, srv::findBundlesByName)
            .flatMapPromise(srv::findComponentsByBundle);
    }

}
