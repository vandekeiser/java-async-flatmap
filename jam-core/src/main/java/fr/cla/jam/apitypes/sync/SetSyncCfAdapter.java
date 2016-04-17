package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SetSyncCfAdapter {

    private final SingleResultSyncCfAdapter singleResultAdapter = new SingleResultSyncCfAdapter();
    private final CollectionSyncCfAdapter collectionResultAdapter = new CollectionSyncCfAdapter();

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee,
        Function<
            Function<T, U>,
            Function<T, CompletableFuture<U>>
        > asyncifier
    ) {
        return singleResultAdapter.adapt(adaptee, asyncifier);
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> adaptee,
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > asyncifier
    ) {
        return collectionResultAdapter.flatMapAdapt(
            inputs, adaptee, asyncifier, Collections::emptySet, Sets::union
        );
    }

}
