package fr.cla.jam.apitypes.sync;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class PoolSetSyncCfAdapter {

    private final SetSyncCfAdapter unpooledSetResultAdapter = new SetSyncCfAdapter();
    private final PoolSingleResultSyncCfAdapter poolSingleResultAdapter;

    public PoolSetSyncCfAdapter(Executor dedicatedPool) {
        this.poolSingleResultAdapter = new PoolSingleResultSyncCfAdapter(dedicatedPool);
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee
    ) {
        return poolSingleResultAdapter.adapt(adaptee);
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> adaptee
    ) {
        return unpooledSetResultAdapter.flatMapAdapt(inputs, adaptee, this::adapt);
    }

}
