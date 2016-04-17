package fr.cla.jam.apitypes.promise.exampledomain;

import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class PromiseCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    public PromiseCfJenkinsPlugin(PromiseJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            PromiseCfAdapter.adapt(srv::findBundlesByName),
            bundles -> PromiseCfAdapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

}
