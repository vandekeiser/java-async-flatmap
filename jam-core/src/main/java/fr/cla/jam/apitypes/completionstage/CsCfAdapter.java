package fr.cla.jam.apitypes.completionstage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class CsCfAdapter {

    public <S, T> Function<S, CompletableFuture<T>> toCompletableFuture(
        Function<S, CompletionStage<T>> mapper
    ) {
        return e -> {
            CompletableFuture<T> result = new CompletableFuture<>();
            mapper.apply(e).whenComplete(
                (t, x) -> {
                    if(x != null) result.completeExceptionally(x);
                    else result.complete(t);
                }
            );
            return result;
        };
    }

}
