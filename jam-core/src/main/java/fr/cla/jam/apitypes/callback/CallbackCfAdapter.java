package fr.cla.jam.apitypes.callback;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public class CallbackCfAdapter {

    public <T, U> Function<T, CompletableFuture<U>> adapt(BiConsumer<T, Callback<U>> adaptee) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            adaptee.accept(input, Callback.either(
                s -> cf.complete(s),
                f -> cf.completeExceptionally(f)
            ));
            return cf;
        };
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        BiConsumer<E, Callback<Set<F>>> adaptee,
        Function<BiConsumer<E, Callback<Set<F>>>, Function<E, CompletableFuture<Set<F>>>> adapter
    ) {
        return inputs.stream()
            .map(adapter.apply(adaptee))
            .collect(toSet())
            .stream()
            .collect(flattening());
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        BiConsumer<E, Callback<Set<F>>> adaptee
    ) {
        return flatMapAdapt(inputs, adaptee, this::adapt);
    }
    
}
