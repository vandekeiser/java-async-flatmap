package fr.cla.jam.apitypes.promise.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;
import fr.cla.jam.apitypes.promise.QuasarPromiseCfAdapter;
import fr.cla.jam.exampledomain.AbstractCfJenkinsPlugin;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JiraBundle;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class QuasarPromiseCfJenkinsPlugin extends AbstractCfJenkinsPlugin implements CfJenkinsPlugin {

    private final static AtomicReference<FiberExecutorScheduler> fiberExecutorScheduler = new AtomicReference<>();
    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    public QuasarPromiseCfJenkinsPlugin(PromiseJiraApi srv, Executor dedicatedPool) {
        super(
            srv,
            QuasarPromiseCfAdapter.adapt(srv::findBundlesByName, dedicatedScheduler(dedicatedPool)),
            bundles -> QuasarPromiseCfAdapter.adaptFlatMap(
                bundles,
                srv::findComponentsByBundle,
                dedicatedScheduler(dedicatedPool)
            )
        );
    }

    private static FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return fiberExecutorScheduler.updateAndGet(fes -> fes!=null ? fes : new FiberExecutorScheduler(
            "QuasarPromiseCfJenkinsPlugin scheduler-" + callInFiberSchedulerCounter.incrementAndGet() , dedicatedPool, MonitorType.JMX, true
        ));
    }

}
