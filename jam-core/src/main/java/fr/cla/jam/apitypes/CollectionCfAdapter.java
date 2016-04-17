package fr.cla.jam.apitypes;

import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningCollectionCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class CollectionCfAdapter {

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>>
    CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, CompletableFuture<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return inputs.stream()
            .map(mapper)
            .collect(toSet())
            .stream()
            .collect(flattening(collectionSupplier, collectionUnion));
    }

}
