package fr.cla.jam.apitypes.callback;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class CallbackCfAdapter {

    public static <T, U> Function<T, CompletableFuture<U>> adapt(BiConsumer<T, Callback<U>> adaptee) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            adaptee.accept(input, Callback.either(
                s -> cf.complete(s),
                f -> cf.completeExceptionally(f)
            ));
            return cf;
        };
    }

    public static <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        BiConsumer<E, Callback<Set<F>>> adaptee,
        Function<
            BiConsumer<E, Callback<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > asyncifier
    ) {
        return inputs.stream()
            .map(asyncifier.apply(adaptee))
            .collect(toSet())
            .stream()
            .collect(flattening());
    }
    
}
