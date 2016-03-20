package fr.cla.jam.nonblocking.completionstage;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class CollectCsApiIntoCf {

    public static <E, F> CompletableFuture<Set<F>> flatMapAsyncUsingPool(
        Set<E> inputs,
        Function<E, CompletionStage<Set<F>>> mapper,
        Executor parallelisationPool
    ) {
        return inputs.stream()
            .map(placeInPoolWhenComplete(mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .collect(flattening());
    }

    public static <E, F> CompletableFuture<Set<F>> flatMapAsync(
        Set<E> inputs,
        Function<E, CompletionStage<Set<F>>> mapper,
        Function<
            Function<E, CompletionStage<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > asyncifier
    ) {
        return inputs.stream()
            .map(asyncifier.apply(mapper))
            .collect(toSet())
            .stream()
            .collect(flattening());    
    }

    public static <S, T> Function<S, CompletableFuture<T>>
    placeInPoolWhenComplete(Function<S, CompletionStage<T>> mapper, Executor pool) {
        return e -> {
            CompletableFuture<T> result = new CompletableFuture<>();
            mapper.apply(e).whenCompleteAsync(
                (t, x) -> {
                    if(x != null) result.completeExceptionally(x);
                    else result.complete(t);
                },
                pool
            );
            return result;
        };
    }
}
