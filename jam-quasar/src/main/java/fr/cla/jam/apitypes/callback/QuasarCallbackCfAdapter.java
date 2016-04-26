package fr.cla.jam.apitypes.callback;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

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

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        BiConsumer<E, Callback<Fs>> adaptee,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return flatMapAdapt(inputs, adaptee, this::adapt, collectionSupplier, collectionUnion);
    }

}
