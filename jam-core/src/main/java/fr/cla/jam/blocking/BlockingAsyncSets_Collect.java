package fr.cla.jam.blocking;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static fr.cla.jam.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class BlockingAsyncSets_Collect {

    public static <E, F> CompletableFuture<Set<F>> flatMapAsync(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Executor parallelisationPool
    ) {
        return inputs.stream()
            .map(BlockingCompletableFutures.asyncifyWithPool(mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .collect(flattening());
    }

    
}
