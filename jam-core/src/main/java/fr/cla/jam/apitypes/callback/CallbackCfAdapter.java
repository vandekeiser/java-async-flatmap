package fr.cla.jam.apitypes.callback;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class CallbackCfAdapter {

    private final CollectionCfAdapter apiTypeAgnosticAdapter = new CollectionCfAdapter();

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        BiConsumer<T, Callback<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            adaptee.accept(input, Callback.either(
                s -> cf.complete(s),
                f -> cf.completeExceptionally(f)
            ));
            return cf;
        };
    }

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        BiConsumer<E, Callback<Fs>> adaptee,
        Function<
            BiConsumer<E, Callback<Fs>>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return apiTypeAgnosticAdapter.flatMapAdapt(inputs, adapter.apply(adaptee), collectionSupplier, collectionUnion);
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
