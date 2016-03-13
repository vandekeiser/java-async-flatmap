package cla.completablefuture.nonblocking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import static java.lang.Thread.currentThread;

public final class CompletableFutures {

    public static <S, T> Function<S, CompletableFuture<T>> 
    asyncify(Function<S, CompletionStage<T>> mapper, Executor pool) {
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

    public static <S, T> Function<S, CompletableFuture<T>>
    asyncify(
            Function<S, CompletionStage<T>> mapper,
            Function<
                    Function<S, CompletionStage<T>>,
                    Function<S, CompletableFuture<T>>
            > asyncifier
    ) {
        return asyncifier.apply(mapper);
    }

}
