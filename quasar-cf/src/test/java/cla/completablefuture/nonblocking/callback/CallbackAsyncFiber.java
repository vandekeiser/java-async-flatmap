package cla.completablefuture.nonblocking.callback;

import co.paralleluniverse.fibers.FiberAsync;

import java.util.function.Function;

public class CallbackAsyncFiber<S, T> extends FiberAsync<T, Throwable> {

    private final Function<S, Callback<T>> instant;

    public CallbackAsyncFiber(Function<S, Callback<T>> instant) {
        this.instant = instant;
    }

    @Override protected void requestAsync() {

    }

}
