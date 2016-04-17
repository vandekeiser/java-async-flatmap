package fr.cla.jam.apitypes.completionstage.exampledomain;

import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.completionstage.QuasarCsCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;

public class QuasarCsCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    private QuasarCsCfJenkinsPlugin(CsJiraApi srv, QuasarCsCfAdapter adapter) {
        super(
            srv,
            adapter.adapt(srv::findBundlesByName),
            bundles -> adapter.flatMapAdapt(bundles, srv::findComponentsByBundle)
        );
    }

    public static QuasarCsCfJenkinsPlugin usingScheduler(CsJiraApi srv, FiberExecutorScheduler dedicatedScheduler) {
        return new QuasarCsCfJenkinsPlugin(srv, new QuasarCsCfAdapter(dedicatedScheduler));
    }

}
