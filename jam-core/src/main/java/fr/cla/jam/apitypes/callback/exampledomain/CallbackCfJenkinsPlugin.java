package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.CallbackCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

import java.util.concurrent.Executor;

public class CallbackCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    private CallbackCfJenkinsPlugin(CallbackJiraApi srv, CallbackCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static CallbackCfJenkinsPlugin using(CallbackJiraApi srv) {
        return new CallbackCfJenkinsPlugin(srv, new CallbackCfAdapter());
    }

}
