package cla.completablefuture.jenkins.nonblocking;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import co.paralleluniverse.fibers.FiberAsync;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class CfFiberAsync<S, T> extends FiberAsync<CompletionStage<T>, CompletionException> {

    private final Function<S, CompletionStage<T>> threadBlockingCall;
    private final S input;

    public CfFiberAsync(Function<S, CompletionStage<T>> threadBlockingCall, S input) {
        this.threadBlockingCall = threadBlockingCall;
        this.input = input;
    }

    @Override
    protected void requestAsync() {
        threadBlockingCall.apply(input).whenComplete((t, x) -> {
            if (x != null) super.asyncFailed(x);
            else super.asyncCompleted(completedFuture(t));
        });
    }

}
