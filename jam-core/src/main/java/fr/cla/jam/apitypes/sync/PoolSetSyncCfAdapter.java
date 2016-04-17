package fr.cla.jam.apitypes.sync;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class PoolSetSyncCfAdapter {

    private final SetSyncCfAdapter notPooled = new SetSyncCfAdapter();
    private final Executor dedicatedPool;

    public PoolSetSyncCfAdapter(Executor dedicatedPool) {
        this.dedicatedPool = dedicatedPool;
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(Function<T, U> adaptee) {
        return notPooled.adapt(
            adaptee,
            mapper -> input -> CompletableFuture.supplyAsync(() -> mapper.apply(input), dedicatedPool)
        );
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> adaptee
    ) {
        return notPooled.flatMapAdapt(
            inputs,
            adaptee,
            mapper -> input -> CompletableFuture.supplyAsync(() -> mapper.apply(input), dedicatedPool)
        );
    }

}
