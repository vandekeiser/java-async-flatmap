package fr.cla.jam.apitypes.callback;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuasarCallbackCfAdapter {

    public static <T, U> Function<
        BiConsumer<T, Callback<U>>,
        Function<T, CompletableFuture<U>>
    > adapt(FiberScheduler dedicatedScheduler) {
        return callback -> input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            new Fiber<>(dedicatedScheduler, () -> {
                callback.accept(input, new Callback<U>() {
                    @Override public void onSuccess(U success) {
                        cf.complete(success);
                    }
                    @Override public void onFailure(Throwable failure) {
                        cf.completeExceptionally(failure);
                    }
                });
            }).start();
            return cf;
        };
    }

}
