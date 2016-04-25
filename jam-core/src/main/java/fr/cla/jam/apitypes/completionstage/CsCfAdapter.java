package fr.cla.jam.apitypes.completionstage;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class CsCfAdapter {

    private final CollectionCfAdapter apiTypeAgnosticAdapter = new CollectionCfAdapter();
    
    public <S, T> Function<S, CompletableFuture<T>> adapt(
        Function<S, CompletionStage<T>> mapper
    ) {
        return e -> {
            CompletableFuture<T> result = new CompletableFuture<>();
            mapper.apply(e).whenComplete(
                (t, x) -> {
                    if(x != null) result.completeExceptionally(x);
                    else result.complete(t);
                }
            );
            return result;
        };
    }

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, CompletionStage<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return flatMapAdapt(inputs, mapper, this::adapt, collectionSupplier, collectionUnion);
    }

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, CompletionStage<Fs>> adaptee,
        Function<
            Function<E, CompletionStage<Fs>>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return apiTypeAgnosticAdapter.flatMapAdapt(
            inputs, 
            adapter.apply(adaptee),
            collectionSupplier,
            collectionUnion
        );
    }

}
