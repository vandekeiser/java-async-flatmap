package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class CallbackCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    public CallbackCfJenkinsPlugin(CallbackJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            CallbackCfAdapter.adapt(srv::findBundlesByName),
            bundles -> CallbackCfAdapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

}
