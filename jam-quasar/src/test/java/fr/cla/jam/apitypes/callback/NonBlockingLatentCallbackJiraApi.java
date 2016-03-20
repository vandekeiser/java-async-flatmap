package fr.cla.jam.apitypes.callback;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import fr.cla.jam.apitypes.callback.exampledomain.AbstractLatentCallbackJiraApi;
import fr.cla.jam.apitypes.callback.exampledomain.CallbackJiraApi;

import java.time.Duration;
import java.time.Instant;

public class NonBlockingLatentCallbackJiraApi extends AbstractLatentCallbackJiraApi {
    private static final FiberScheduler delayScheduler = new FiberExecutorScheduler("delay scheduler", delayExecutor);

    public NonBlockingLatentCallbackJiraApi(CallbackJiraApi jira) {
        super(jira);
    }

    @Override
    protected <I, O> void sleepThenPropagateSuccess(I i, O success, Callback<O> c) {
        new Fiber<Void>(delayScheduler, () -> {
            //Instant beforeSleep = Instant.now();
            doSleepRandomlyForRequest(i);
            c.onSuccess(success);
            //System.out.println("____realSleep: " + Duration.between(beforeSleep, Instant.now()));
        }).start();
    }

    private void doSleepRandomlyForRequest(Object request) throws SuspendExecution {
        sleep(sleepDuration(request));
        //oldsleep(sleepDuration(request));
    }

    private void sleep(long sleepInMillis) throws SuspendExecution {
        try {
            Fiber.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            Strand.currentStrand().interrupt();
        }
    }

//    private void oldsleep(long sleepInMillis) throws SuspendExecution {
//        try {
//            Thread.sleep(sleepInMillis);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }

}
