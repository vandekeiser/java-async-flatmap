package fr.cla.jam.apitypes.promise.exampledomain;

import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class PromiseCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    private PromiseCfJenkinsPlugin(PromiseJiraApi srv, PromiseCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static PromiseCfJenkinsPlugin using(PromiseJiraApi srv) {
        return new PromiseCfJenkinsPlugin(srv, new PromiseCfAdapter());
    }

}
