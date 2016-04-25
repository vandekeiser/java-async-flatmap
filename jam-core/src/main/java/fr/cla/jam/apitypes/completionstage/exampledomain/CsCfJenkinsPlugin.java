package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.Csf;
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
    public Csf<JiraComponent> findComponentsByBundleNameAsync(String bundleName) {
        return Csf
            .ofCs(bundleName, srv::findBundlesByName)
            .flatMapCs(srv::findComponentsByBundle);
    }

}
