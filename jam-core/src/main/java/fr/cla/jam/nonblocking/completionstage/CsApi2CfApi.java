package fr.cla.jam.nonblocking.completionstage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class CsApi2CfApi {

    public static <S, T> Function<S, CompletableFuture<T>>
    placeInPoolWhenComplete(Function<S, CompletionStage<T>> mapper, Executor pool) {
        return e -> {
            CompletableFuture<T> result = new CompletableFuture<>();
            mapper.apply(e).whenCompleteAsync(
                (t, x) -> {
                    if(x != null) result.completeExceptionally(x);
                    else result.complete(t);
                },
                pool
            );
            return result;
        };
    }

}
