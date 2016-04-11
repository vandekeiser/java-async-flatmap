package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class SyncCfAdapter {

    public static <T, U> Function<T, CompletableFuture<U>> adaptUsingPool(
        Function<T, U> adaptee,
        Executor parallelisationPool
    ) {
        return adapt(
            adaptee,
            a -> CompletableFuture.supplyAsync(a, parallelisationPool)
        );
    }
    
    public static <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee,
        Function<Supplier<U>, CompletableFuture<U>> asyncifier
    ) {
        Function<T, U> verifiedAdaptee = requireNonNull(adaptee);
        return t -> asyncifier.apply(
            () -> verifiedAdaptee.apply(t)
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
