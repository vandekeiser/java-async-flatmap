package fr.cla.jam.exampledomain.apitypes.callback;

import fr.cla.jam.apitypes.callback.Callback;

import java.util.function.BiConsumer;

public class BlockingLatentCallbackJiraApi extends AbstractLatentCallbackJiraApi {

    public BlockingLatentCallbackJiraApi(CallbackJiraApi jira) {
        super(jira);
    }

    @Override
    protected <I, O> BiConsumer<I, Callback<O>> delay(BiConsumer<I, Callback<O>> instant) {
        return (i, c) -> instant.accept(i, Callback.either(
            success -> sleepThenPropagateSuccess(i, success, c),
            failure -> c.onFailure(failure)
        ));
    }

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

    @Override public String description() {
        return getClass().getSimpleName();
    }

}
