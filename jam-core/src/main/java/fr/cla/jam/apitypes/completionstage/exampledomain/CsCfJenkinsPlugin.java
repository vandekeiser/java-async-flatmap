package fr.cla.jam.apitypes.completionstage.exampledomain;

import fr.cla.jam.apitypes.completionstage.CsCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class CsCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {
    
    public CsCfJenkinsPlugin(CsJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            CsCfAdapter.adapt(srv::findBundlesByName, dedicatedPool),
            bundles -> CsCfAdapter.flatMapAdaptUsingPool(bundles, srv::findComponentsByBundle, dedicatedPool)
        );
    }

}
