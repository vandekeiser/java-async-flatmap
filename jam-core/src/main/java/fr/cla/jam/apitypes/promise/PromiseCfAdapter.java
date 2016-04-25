package fr.cla.jam.apitypes.promise;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class PromiseCfAdapter {

    private final CollectionCfAdapter apiTypeAgnosticAdapter = new CollectionCfAdapter();

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, Promise<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            adaptee.apply(input).whenComplete(
                res -> cf.complete(res),
                x -> cf.completeExceptionally(x)
            );
            return cf;
        };
    }

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, Promise<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return flatMapAdapt(inputs, mapper, this::adapt, collectionSupplier, collectionUnion);
    }

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, Promise<Fs>> adaptee,
        Function<
            Function<E, Promise<Fs>>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return apiTypeAgnosticAdapter.flatMapAdapt(inputs, adapter.apply(adaptee), collectionSupplier, collectionUnion);
    }

}
