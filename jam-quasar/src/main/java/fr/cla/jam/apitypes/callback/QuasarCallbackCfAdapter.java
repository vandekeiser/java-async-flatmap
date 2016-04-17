package fr.cla.jam.apitypes.callback;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.apitypes.promise.PromiseCfAdapter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public class QuasarCallbackCfAdapter extends CallbackCfAdapter {

    private final FiberScheduler dedicatedScheduler;

    public QuasarCallbackCfAdapter(FiberScheduler dedicatedScheduler) {
        this.dedicatedScheduler = dedicatedScheduler;
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        BiConsumer<T, Callback<U>> adaptee
    ) {
        return input -> {
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

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        BiConsumer<E, Callback<Set<F>>> adaptee
    ) {
        return flatMapAdapt(inputs, adaptee, this::adapt);
    }

}
