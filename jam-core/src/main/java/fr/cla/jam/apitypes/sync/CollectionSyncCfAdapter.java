package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import static fr.cla.jam.util.collectors.FlatteningCollectionCollector.flattening;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public final class CollectionSyncCfAdapter {

    private final SingleResultSyncCfAdapter singleResultAdapter = new SingleResultSyncCfAdapter();

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, Fs> mapper,
        Function<Supplier<Fs>, CompletableFuture<Fs>> asyncifier,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return inputs.stream()
            .map(singleResultAdapter.adapt(mapper, asyncifier))
            .collect(toSet())
            .stream()
            .collect(flattening(collectionSupplier, collectionUnion));    
    }

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt2(
        Es inputs,
        Function<E, Fs> mapper,
        Function<
            Function<E, Fs>,
            Function<E, CompletableFuture<Fs>>
        > asyncifier,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return inputs.stream()
            .map(singleResultAdapter.adapt2(mapper, asyncifier))
            .collect(toSet())
            .stream()
            .collect(flattening(collectionSupplier, collectionUnion));
    }

}
