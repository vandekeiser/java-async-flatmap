package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SetSyncCfAdapter {

    private final SingleResultSyncCfAdapter singleResultAdapter = new SingleResultSyncCfAdapter();
    private final CollectionSyncCfAdapter collectionResultAdapter = new CollectionSyncCfAdapter();

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee,
        Function<Supplier<U>, CompletableFuture<U>> asyncifier
    ) {
        return singleResultAdapter.adapt(adaptee, asyncifier);
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt2(
        Function<T, U> adaptee,
        Function<
            Function<T, U>,
            Function<T, CompletableFuture<U>>
        > asyncifier
    ) {
        return singleResultAdapter.adapt2(adaptee, asyncifier);
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Function<Supplier<Set<F>>, CompletableFuture<Set<F>>> asyncifier
    ) {
        return collectionResultAdapter.flatMapAdapt(
            inputs, mapper, asyncifier, Collections::emptySet, Sets::union
        );
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt2(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > asyncifier
    ) {
        return collectionResultAdapter.flatMapAdapt2(
                inputs, mapper, asyncifier, Collections::emptySet, Sets::union
        );
    }

}
