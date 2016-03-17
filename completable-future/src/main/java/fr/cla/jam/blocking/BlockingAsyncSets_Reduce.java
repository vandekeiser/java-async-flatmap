package fr.cla.jam.blocking;

import fr.cla.jam.Sets;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toSet;

public final class BlockingAsyncSets_Reduce {

    public static <E, F> CompletableFuture<Set<F>> flatMapAsync(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Executor parallelisationPool
    ) {
        Function<E, Set<F>> _mapper = requireNonNull(mapper);
        
        return inputs.stream()
            .map(BlockingCompletableFutures.asyncifyWithPool(_mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .reduce(
                completedFuture(emptySet()),
                (cf1, cf2) -> cf1.thenCombine(cf2, Sets::union)
            );    
    }

}