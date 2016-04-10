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
        return adaptee -> input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();

            new Fiber<>(dedicatedScheduler, () -> {
                adaptee.accept(input, Callback.either(
                    s -> cf.complete(s),
                    f -> cf.completeExceptionally(f)
                ));
            }).start();

            return cf;
        };
    }

}
