package fr.cla.jam.apitypes.sync;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SingleResultSyncCfAdapter {

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee,
        Function<
            Function<T, U>,
            Function<T, CompletableFuture<U>>
        > adapter
    ) {
        return adapter.apply(adaptee);
    }

}
