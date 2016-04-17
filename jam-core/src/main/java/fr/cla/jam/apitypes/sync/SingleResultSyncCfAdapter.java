package fr.cla.jam.apitypes.sync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class SingleResultSyncCfAdapter {

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, U> adaptee,
        Function<Supplier<U>, CompletableFuture<U>> asyncifier
    ) {
        Function<T, U> verifiedAdaptee = requireNonNull(adaptee);
        return t -> asyncifier.apply(
            () -> verifiedAdaptee.apply(t)
        );
    }

}
