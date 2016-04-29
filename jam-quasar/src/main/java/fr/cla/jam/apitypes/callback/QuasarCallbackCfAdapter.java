package fr.cla.jam.apitypes.callback;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuasarCallbackCfAdapter extends CallbackCfAdapter {

    private final FiberScheduler quasar;

    public QuasarCallbackCfAdapter(FiberScheduler quasar) {
        this.quasar = quasar;
    }

    public <T, U> Function<T, CompletableFuture<U>> toCompletableFuture(
        BiConsumer<T, Callback<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();

            new Fiber<>(quasar, () -> {
                adaptee.accept(input, Callback.either(
                    s -> cf.complete(s),
                    f -> cf.completeExceptionally(f)
                ));
            }).start();

            return cf;
        };
    }

}
