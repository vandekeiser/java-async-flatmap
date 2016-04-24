package fr.cla.jam.apitypes.promise.exampledomain;

import fr.cla.jam.Csf;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

public class PromiseCfJenkinsPlugin2 extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final PromiseJiraApi srv;

    private PromiseCfJenkinsPlugin2(PromiseJiraApi srv) {
        super(srv);
        this.srv = srv;
    }

    @Override
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return Csf
            .ofPromise(bundleName, srv::findBundlesByName)
            .flatMapPromise(srv::findComponentsByBundle);
    }

}
