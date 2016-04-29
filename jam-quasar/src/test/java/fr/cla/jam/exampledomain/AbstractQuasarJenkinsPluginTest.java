package fr.cla.jam.exampledomain;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public abstract class AbstractQuasarJenkinsPluginTest extends AbstractJenkinsPluginTest {

    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    protected final FiberExecutorScheduler toQuasar(Executor pool) {
        return new FiberExecutorScheduler(getSchedulerName(), pool, MonitorType.JMX, true);
    }

    private String getSchedulerName() {
        return format("%s scheduler-%d",
            getClass().getSimpleName(),
            callInFiberSchedulerCounter.incrementAndGet()
        );
    }

}
