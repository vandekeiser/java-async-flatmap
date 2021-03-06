package fr.cla.jam.exampledomain.apitypes.completionstage;

import fr.cla.jam.CfOfSet;
import fr.cla.jam.exampledomain.AbstractJenkinsPlugin;
import fr.cla.jam.exampledomain.CsfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraComponent;

public class CsCfJenkinsPlugin extends AbstractJenkinsPlugin implements CsfJenkinsPlugin {

    private final CsJiraApi srv;

    public CsCfJenkinsPlugin(CsJiraApi srv) {
        super(srv);
        this.srv = srv;
    }

    @Override
    public CfOfSet<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return CfOfSet
            .ofCs(bundleName, srv::findBundlesByName)
            .flatMapCs(srv::findComponentsByBundle);
    }

}
