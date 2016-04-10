package fr.cla.jam.apitypes.promise;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class PromiseCfAdapter {

    public static <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, Promise<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            adaptee.apply(input).whenComplete(
                res -> cf.complete(res),
                x -> cf.completeExceptionally(x)
            );
            return cf;
        };
    }

    public static <E, F> CompletableFuture<Set<F>> adaptFlatMap(
        Set<E> inputs,
        Function<E, Promise<Set<F>>> mapper
    ) {
        return adaptFlatMap(inputs, mapper, PromiseCfAdapter::adapt);
    }

    public static <E, F> CompletableFuture<Set<F>> adaptFlatMap(
        Set<E> inputs,
        Function<E, Promise<Set<F>>> mapper,
        Function<
            Function<E, Promise<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return inputs.stream()
            .map(adapter.apply(mapper))
            .collect(toSet())
            .stream()
            .collect(flattening());
    }

}