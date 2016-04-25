package fr.cla.jam.apitypes;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.exampledomain.AbstractJenkinsPluginTest;
import fr.cla.jam.exampledomain.AbstractJenkinsPluginTest2;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public abstract class AbstractQuasarJenkinsPluginTest extends AbstractJenkinsPluginTest2 {

    private final static AtomicInteger callInFiberSchedulerCounter = new AtomicInteger(0);

    protected final FiberExecutorScheduler dedicatedScheduler(Executor dedicatedPool) {
        return new FiberExecutorScheduler(getSchedulerName(), dedicatedPool, MonitorType.JMX, true);
    }

    private String getSchedulerName() {
        return format("%s scheduler-%d",
            getClass().getSimpleName(),
            callInFiberSchedulerCounter.incrementAndGet()
        );
    }

}
