package fr.cla.jam.apitypes.callback;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuasarCallbackApi2CfApi {

    public static <T, U> Function<
        BiConsumer<T, Callback<U>>,
        Function<T, CompletableFuture<U>>
    > usingFiberScheduler(FiberScheduler dedicatedScheduler) {
        return callback -> input -> cfThatWaitsToBeCalledBack(callback, input, dedicatedScheduler);
    }

    private static <T, U> CompletableFuture<U> cfThatWaitsToBeCalledBack(
        BiConsumer<T, Callback<U>> call,
        T input,
        FiberScheduler dedicatedScheduler
    ) {
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        new Fiber<>(dedicatedScheduler, () -> {
            call.accept(input, new Callback<U>() {
                @Override public void onSuccess(U success) {
                    fiberCf.complete(success);
                }
                @Override public void onFailure(Throwable failure) {
                    fiberCf.completeExceptionally(failure);
                }
            });
        }).start();

        return fiberCf;
    }

}
