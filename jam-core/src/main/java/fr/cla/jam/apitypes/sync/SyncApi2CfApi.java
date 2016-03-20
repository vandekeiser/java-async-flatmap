package fr.cla.jam.apitypes.sync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class SyncApi2CfApi {

    private static <T> Function<Supplier<T>, CompletableFuture<T>> jdkAsyncifier(Executor parallelisationPool) {
        return s -> CompletableFuture.supplyAsync(s, parallelisationPool);
    }
    
    public static <T, U> Function<T, CompletableFuture<U>> asyncifyWithPool(
        Function<T, U> f,
        Executor parallelisationPool
    ) {
        return asyncify(f, jdkAsyncifier(parallelisationPool));
    }
    
    public static <T, U> Function<T, CompletableFuture<U>> asyncify(
        Function<T, U> f,
        Function<Supplier<U>, CompletableFuture<U>> asyncifier
    ) {
        Function<T, U> _f = requireNonNull(f);
        
        return t -> asyncifier.apply(
            () -> _f.apply(t)
        );
    }
    
}
