package fr.cla.jam.nonblocking.promise;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static fr.cla.jam.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class PromiseAsyncSets_Collect {

    public static <E, F> CompletableFuture<Set<F>> flatMapPromiseAsync(
        Set<E> inputs,
        Function<E, Promise<Set<F>>> mapper,
        Function<
            Function<E, Promise<Set<F>>>,
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
