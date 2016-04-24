package fr.cla.jam.apitypes.promise;

import fr.cla.jam.apitypes.SetCfAdapter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PromiseCfAdapter {

    private final SetCfAdapter apiTypeAgnosticAdapter = new SetCfAdapter();

    public <T, U> Function<T, CompletableFuture<U>> adapt(
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

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Promise<Set<F>>> mapper
    ) {
        return flatMapAdapt(inputs, mapper, this::adapt);
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Promise<Set<F>>> adaptee,
        Function<
            Function<E, Promise<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return apiTypeAgnosticAdapter.flatMapAdapt(inputs, adapter.apply(adaptee));
    }

}
