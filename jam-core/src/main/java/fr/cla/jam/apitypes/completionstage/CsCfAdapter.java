package fr.cla.jam.apitypes.completionstage;

import fr.cla.jam.apitypes.SetCfAdapter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public class CsCfAdapter {

    private final SetCfAdapter apiTypeAgnosticAdapter = new SetCfAdapter();

    public <S, T> Function<S, CompletableFuture<T>> adapt(
        Function<S, CompletionStage<T>> mapper
    ) {
        return e -> {
            CompletableFuture<T> result = new CompletableFuture<>();
            mapper.apply(e).whenCompleteAsync(
                (t, x) -> {
                    if(x != null) result.completeExceptionally(x);
                    else result.complete(t);
                }
            );
            return result;
        };
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, CompletionStage<Set<F>>> mapper
    ) {
        return flatMapAdapt(inputs, mapper, this::adapt);
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, CompletionStage<Set<F>>> adaptee,
        Function<
            Function<E, CompletionStage<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return apiTypeAgnosticAdapter.flatMapAdapt(inputs, adapter.apply(adaptee));
    }

}
