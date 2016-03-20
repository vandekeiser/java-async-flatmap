package fr.cla.jam.apitypes.promise;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class CollectPromiseApiIntoCf {

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

    public static <E, F> CompletableFuture<Set<F>> flatMapPromiseAsyncUsingPool(
            Set<E> inputs,
            Function<E, Promise<Set<F>>> mapper,
            Executor dedicatedPool
    ) {
        return flatMapPromiseAsync(inputs, mapper, PromiseApi2CfApi.waitToBeCalledBack(dedicatedPool));
    }


}
