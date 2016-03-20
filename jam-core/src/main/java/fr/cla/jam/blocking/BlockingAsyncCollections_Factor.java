package fr.cla.jam.blocking;

import fr.cla.jam.CollectionContainerOfMany;
import fr.cla.jam.CollectionSupplier;
import fr.cla.jam.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class BlockingAsyncCollections_Factor {

    public static <E, F> CompletableFuture<Set<F>> flatMapSetAsync(
        Set<E> inputs,
        Function<E, Set<F>> mapper,
        Executor parallelisationPool
    ) {
        return flatMapCollectionAsync(inputs, mapper, parallelisationPool, Collections::emptySet, Sets::union);
    }
    
    public static <E, Es extends Collection<E>, F, Fs extends Collection<F>> 
    CompletableFuture<Fs> flatMapCollectionAsync(
        Es inputs,
        Function<E, Fs> mapper,
        Executor parallelisationPool,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return BlockingAsyncContainersOfMany.flatMapAsync(
            new CollectionContainerOfMany<>(inputs),
            mapper.andThen(CollectionContainerOfMany::new),
            parallelisationPool,
            CollectionContainerOfMany.containerSupplier(collectionSupplier),
            CollectionContainerOfMany.containerUnion(collectionUnion)
        ).thenApply(
            CollectionContainerOfMany::underlyingContainer
        );
    }


}
