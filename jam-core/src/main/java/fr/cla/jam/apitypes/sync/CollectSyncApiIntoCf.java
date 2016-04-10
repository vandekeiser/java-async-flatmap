package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.collectors.FlatteningCollectionCollector;
import fr.cla.jam.util.containers.CollectionSupplier;
import fr.cla.jam.util.containers.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

public final class CollectSyncApiIntoCf {

    public static <E, F> CompletableFuture<Set<F>> flatMapSetAsync(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Executor parallelisationPool
    ) {
        return flatMapCollectionAsync(inputs, mapper, parallelisationPool, Collections::emptySet, Sets::union);
    }
    
    public static <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapCollectionAsync(
        Es inputs,
        Function<E, Fs> mapper,
        Executor parallelisationPool,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return inputs.stream()
            .map(SyncCfAdapter.adaptUsingPool(mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .collect(FlatteningCollectionCollector.flattening(collectionSupplier, collectionUnion));
    }

}
