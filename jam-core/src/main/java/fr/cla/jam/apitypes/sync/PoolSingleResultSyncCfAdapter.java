package fr.cla.jam.apitypes.sync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class PoolSingleResultSyncCfAdapter {

    private final Executor pool;

    public PoolSingleResultSyncCfAdapter(Executor pool) {
        this.pool = pool;
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee
    ) {
        return input -> CompletableFuture.supplyAsync(
            () -> adaptee.apply(input), pool
        );
    }
    
}
