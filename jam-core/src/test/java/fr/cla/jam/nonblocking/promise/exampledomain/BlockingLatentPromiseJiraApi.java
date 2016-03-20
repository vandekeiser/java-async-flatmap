package fr.cla.jam.nonblocking.promise.exampledomain;

import fr.cla.jam.nonblocking.promise.CompletablePromise;

public class BlockingLatentPromiseJiraApi extends AbstractLatentPromiseJiraApi implements PromiseJiraApi {

    public BlockingLatentPromiseJiraApi(PromiseJiraApi jira) {
        super(jira);
    }

    @Override
    protected <I, O> void sleepThenPropagateSuccess(I i, O success, CompletablePromise<O> c) {
        delayExecutor.execute(() -> {
            doSleepRandomlyForRequest(i);
            c.complete(success);
        });
    }

    private void doSleepRandomlyForRequest(Object request) {
        sleep(sleepDuration(request));
    }

    private void sleep(long sleepInMillis) {
        try {
            Thread.sleep(sleepInMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
