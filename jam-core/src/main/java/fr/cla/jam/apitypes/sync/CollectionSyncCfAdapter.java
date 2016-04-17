package fr.cla.jam.apitypes.sync;

import fr.cla.jam.apitypes.CollectionCfAdapter;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class CollectionSyncCfAdapter {

    private final CollectionCfAdapter apiTypeAgnosticAdapter = new CollectionCfAdapter();

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, Fs> mapper,
        Function<
            Function<E, Fs>,
            Function<E, CompletableFuture<Fs>>
        > adapter,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return apiTypeAgnosticAdapter.flatMapAdapt(
            inputs,
            adapter.apply(mapper),
            collectionSupplier,
            collectionUnion
        );
    }

}
