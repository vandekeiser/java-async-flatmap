package fr.cla.jam.apitypes;

import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import fr.cla.jam.apitypes.callback.NonBlockingLatentCallbackJiraApi;
import fr.cla.jam.apitypes.callback.exampledomain.*;
import fr.cla.jam.exampledomain.AbstractJenkinsPluginTest;
import fr.cla.jam.exampledomain.CfJenkinsPlugin;
import fr.cla.jam.exampledomain.JenkinsPlugin;
import org.junit.After;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public abstract class AbstractQuasarJenkinsPluginTest extends AbstractJenkinsPluginTest {

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
