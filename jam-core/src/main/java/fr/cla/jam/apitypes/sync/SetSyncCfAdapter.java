package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SetSyncCfAdapter {

    public static <E, F> CompletableFuture<Set<F>> flatMapAdaptUsingPool(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Executor pool
    ) {
        return flatMapAdapt(
            inputs,
            mapper,
            resultSupplier -> CompletableFuture.supplyAsync(resultSupplier, pool)
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
