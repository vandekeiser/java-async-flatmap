package fr.cla.jam.nonblocking.callback;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static fr.cla.jam.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class CallbackAsyncSets_Collect {

    public static <E, F> CompletableFuture<Set<F>> flatMapCallbackAsync(
        Set<E> inputs,
        BiConsumer<E, Callback<Set<F>>> mapper,
        Function<
            BiConsumer<E, Callback<Set<F>>>,
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
