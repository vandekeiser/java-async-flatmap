package fr.cla.jam.nonblocking.callback;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import fr.cla.jam.nonblocking.callback.exampledomain.CallbackJiraApi;

public class NonBlockingCallbackJiraApiWithLatency extends AbstractCallbackJiraApiWithLatency {
    //private static final Executor delayExecutor = Executors.newCachedThreadPool();
    private static final FiberScheduler delayScheduler = new FiberExecutorScheduler("delay scheduler", delayExecutor);

    public NonBlockingCallbackJiraApiWithLatency(CallbackJiraApi jira) {
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
