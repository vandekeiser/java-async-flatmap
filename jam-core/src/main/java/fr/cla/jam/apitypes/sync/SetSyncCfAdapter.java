package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SetSyncCfAdapter {

    private final Executor dedicatedPool;

    public SetSyncCfAdapter(Executor dedicatedPool) {
        this.dedicatedPool = dedicatedPool;
    }

    public <T, U> Function<T, CompletableFuture<U>> adaptUsingPool(Function<T, U> adaptee) {
        return SyncCfAdapter.adapt(
            adaptee,
            resultSupplier -> CompletableFuture.supplyAsync(resultSupplier, dedicatedPool)
        );
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdaptUsingPool(
        Set<E> inputs,
        Function<E, Set<F>> mapper
    ) {
        return flatMapAdapt(
            inputs,
            mapper,
            resultSupplier -> CompletableFuture.supplyAsync(resultSupplier, dedicatedPool)
        );
    }

    public static <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Function<Supplier<Set<F>>, CompletableFuture<Set<F>>> asyncifier
    ) {
        return CollectionSyncCfAdapter.flatMapAdapt(
            inputs, mapper, asyncifier, Collections::emptySet, Sets::union
        );
    }

}
