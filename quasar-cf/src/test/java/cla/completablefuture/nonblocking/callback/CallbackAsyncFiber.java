package cla.completablefuture.nonblocking.callback;

import java.util.function.Function;
import cla.completablefuture.jira.nonblocking.callback.Callback;
import co.paralleluniverse.fibers.FiberAsync;

public class CallbackAsyncFiber<S, T> extends FiberAsync<T, Throwable> {

    private final Function<S, Callback<T>> instant;

    public CallbackAsyncFiber(Function<S, Callback<T>> instant) {
        this.instant = instant;
    }

    @Override protected void requestAsync() {

    }

}
