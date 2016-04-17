package fr.cla.jam.apitypes.sync;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class PoolSetSyncCfAdapter extends SetSyncCfAdapter {

    private final Executor dedicatedPool;

    public PoolSetSyncCfAdapter(Executor dedicatedPool) {
        this.dedicatedPool = dedicatedPool;
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(Function<T, U> adaptee) {
        return adapt(
            adaptee,
            resultSupplier -> CompletableFuture.supplyAsync(resultSupplier, dedicatedPool)
        );
    }

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> mapper
    ) {
        return flatMapAdapt(
            inputs,
            mapper,
            resultSupplier -> CompletableFuture.supplyAsync(resultSupplier, dedicatedPool)
        );
    }

}
