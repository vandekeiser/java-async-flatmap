package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import static fr.cla.jam.util.collectors.FlatteningCollectionCollector.flattening;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public final class CollectionSyncCfAdapter {

    public static <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee,
        Function<Supplier<U>, CompletableFuture<U>> asyncifier
    ) {
        Function<T, U> verifiedAdaptee = requireNonNull(adaptee);
        return t -> asyncifier.apply(
            () -> verifiedAdaptee.apply(t)
        );
    }

    public static <T, U> Function<T, CompletableFuture<U>> adaptUsingPool(
        Function<T, U> adaptee,
        Executor pool
    ) {
        return t -> CompletableFuture.supplyAsync(
            () -> adaptee.apply(t),
            pool
        );
    }

    public static <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdaptUsingPool(
        Es inputs,
        Function<E, Fs> mapper,
        Executor pool,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return flatMapAdapt(
            inputs,
            mapper,
            resultSupplier -> CompletableFuture.supplyAsync(resultSupplier, pool),
            collectionSupplier,
            collectionUnion
        );
    }

    public static <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, Fs> mapper,
        Function<Supplier<Fs>, CompletableFuture<Fs>> asyncifier,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return inputs.stream()
            .map(adapt(mapper, asyncifier))
            .collect(toSet())
            .stream()
            .collect(flattening(collectionSupplier, collectionUnion));    
    }

}
