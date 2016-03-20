package fr.cla.jam.apitypes.callback.exampledomain;

import fr.cla.jam.apitypes.callback.Callback;

public class BlockingLatentCallbackJiraApi extends AbstractLatentCallbackJiraApi {

    public BlockingLatentCallbackJiraApi(CallbackJiraApi jira) {
        super(jira);
    }

    @Override
    protected <I, O> void sleepThenPropagateSuccess(I input, O success, Callback<O> c) {
        delayExecutor.execute(() -> {
            sleepRandomlyForRequest(input);
            c.onSuccess(success);
        });
    }

    private void sleepRandomlyForRequest(Object request) {
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
