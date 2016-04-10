package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class SetSyncCfAdapter {

    public static <E, F> CompletableFuture<Set<F>> flatMapAdaptUsingPool(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Executor parallelisationPool
    ) {
        return CollectionSyncCfAdapter.flatMapAdaptUsingPool(inputs, mapper, parallelisationPool, Collections::emptySet, Sets::union);
    }
    
}
