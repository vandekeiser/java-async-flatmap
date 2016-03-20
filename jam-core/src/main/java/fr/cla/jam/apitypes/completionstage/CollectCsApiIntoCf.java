package fr.cla.jam.apitypes.completionstage;

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
            .map(CsApi2CfApi.placeInPoolWhenComplete(mapper, parallelisationPool))
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

}
