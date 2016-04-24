package fr.cla.jam.apitypes.sync;

import fr.cla.jam.apitypes.SetCfAdapter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SetSyncCfAdapter {

    private final SetCfAdapter apiTypeAgnosticAdapter = new SetCfAdapter();

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Set<F>> adaptee,
        Function<
            Function<E, Set<F>>,
            Function<E, CompletableFuture<Set<F>>>
        > adapter
    ) {
        return apiTypeAgnosticAdapter.flatMapAdapt(
            inputs, adapter.apply(adaptee)
        );
    }

}
