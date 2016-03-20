package fr.cla.jam.blocking;

import fr.cla.jam.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import static fr.cla.jam.FlatteningCollectionCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class BlockingAsyncCollectionsWithGenericAsyncifier {

    public interface CollectionSupplier<E, Es extends Collection<E>> extends Supplier<Es> {}
    
    public static <E, F> CompletableFuture<Set<F>> flatMapSetAsync(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Function<Supplier<Set<F>>, CompletableFuture<Set<F>>> asyncifier
    ) {
        return flatMapCollectionAsync(inputs, mapper, asyncifier, Collections::emptySet, Sets::union);    
    }
    
    public static <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapCollectionAsync(
        Es inputs,
        Function<E, Fs> mapper,
        Function<Supplier<Fs>, CompletableFuture<Fs>> asyncifier,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return inputs.stream()
            .map(BlockingCompletableFutures.asyncify(mapper, asyncifier))
            .collect(toSet())
            .stream()
            .collect(flattening(collectionSupplier, collectionUnion));    
    }

}
