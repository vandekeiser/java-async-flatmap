package fr.cla.jam.nonblocking.callback;

import co.paralleluniverse.fibers.*;
import co.paralleluniverse.strands.SuspendableRunnable;
import fr.cla.jam.exampledomain.JiraBundle;
import fr.cla.jam.exampledomain.JiraComponent;
import fr.cla.jam.nonblocking.callback.exampledomain.CallbackJiraServer;
import co.paralleluniverse.strands.Strand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NonBlockingCallbackJiraServerWithLatency extends AbstractCallbackJiraServerWithLatency {
    //private static final Executor delayExecutor = Executors.newCachedThreadPool();
    private static final FiberScheduler delayScheduler = new FiberExecutorScheduler("delay scheduler", delayExecutor);

    public NonBlockingCallbackJiraServerWithLatency(CallbackJiraServer jira) {
        super(jira);
    }

    @Override
    protected <I, O> void sleepThenPropagateSuccess(I i, O success, Callback<O> c) {
        new Fiber<Void>(delayScheduler, () -> {
            doSleepRandomlyForRequest(i);
            c.onSuccess(success);
        }).start();
    }

    private void doSleepRandomlyForRequest(Object request) throws SuspendExecution {
        sleep(sleepDuration(request));
    }

    private void sleep(long sleepInMillis) throws SuspendExecution {
        try {
            Fiber.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            Strand.currentStrand().interrupt();
        }
    }

}
