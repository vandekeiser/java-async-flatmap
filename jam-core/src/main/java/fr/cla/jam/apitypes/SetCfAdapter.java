package fr.cla.jam.apitypes;

import fr.cla.jam.util.containers.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SetCfAdapter {

    private final CollectionCfAdapter collectionResultAdapter = new CollectionCfAdapter();

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, CompletableFuture<Set<F>>> mapper
    ) {
        return collectionResultAdapter.flatMapAdapt(
            inputs, mapper, Collections::emptySet, Sets::union
        );
    }

}
