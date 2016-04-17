package fr.cla.jam.apitypes.sync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class PoolSingleResultSyncCfAdapter {

    private final SingleResultSyncCfAdapter notPooled = new SingleResultSyncCfAdapter();
    private final Executor pool;

    public PoolSingleResultSyncCfAdapter(Executor pool) {
        this.pool = pool;
    }

    public <T, U> Function<T, CompletableFuture<U>> adaptUsingPool(
        Function<T, U> adaptee
    ) {
        return notPooled.adapt(
            adaptee,
            resultSupplier -> CompletableFuture.supplyAsync(resultSupplier, pool)
        );
    }
    
}
