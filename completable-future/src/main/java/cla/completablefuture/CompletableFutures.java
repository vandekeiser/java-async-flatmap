package cla.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class CompletableFutures {

    public static <T, U> Function<T, CompletableFuture<U>> asyncify(
        Function<T, U> f,
        Executor parallelisationPool
    ) {
        Function<T, U> _f = requireNonNull(f);
        
        return t -> CompletableFuture.supplyAsync(
            () -> _f.apply(t),
            parallelisationPool
        );
    }
    
}
