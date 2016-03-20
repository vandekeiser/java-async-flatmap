package fr.cla.jam.apitypes.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import fr.cla.jam.apitypes.promise.exampledomain.AbstractLatentPromiseJiraApi;
import fr.cla.jam.apitypes.promise.exampledomain.PromiseJiraApi;

public class NonBlockingLatentPromiseJiraApi extends AbstractLatentPromiseJiraApi implements PromiseJiraApi {

    private static final FiberScheduler delayScheduler = new FiberExecutorScheduler("delay scheduler", delayExecutor);

    public NonBlockingLatentPromiseJiraApi(PromiseJiraApi jira) {
        super(jira);
    }

    @Override
    protected <I, O> void sleepThenPropagateSuccess(I i, O success, CompletablePromise<O> c) {
        new Fiber<>(delayScheduler, () -> {
            doSleepRandomlyForRequest(i);
            c.complete(success);
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
