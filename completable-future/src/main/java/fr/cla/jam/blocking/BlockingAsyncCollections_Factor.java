package fr.cla.jam.blocking;

import fr.cla.jam.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class BlockingAsyncCollections_Factor {

    public interface CollectionSupplier<E, Es extends Collection<E>> 
    extends Supplier<Es> {}
    
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
    
    
    private static class CollectionContainerOfMany<E, Es extends Collection<E>> 
    implements BlockingAsyncContainersOfMany.ContainerOfMany<E> {
        private final Es coll;
        private CollectionContainerOfMany(Es coll) {this.coll = requireNonNull(coll);}
        private Es underlyingContainer() {return this.coll;}
        
        @Override public Stream<E> stream() {
            return underlyingContainer().stream();
        }
        
        static <F, Fs extends Collection<F>> 
        BinaryOperator<CollectionContainerOfMany<F, Fs>> containerUnion(
            BinaryOperator<Fs> collectionUnion
        ) {
            return (c1, c2) -> new CollectionContainerOfMany<>(collectionUnion.apply(
                c1.underlyingContainer(), c2.underlyingContainer()
            ));           
        }
        
        static <F, Fs extends Collection<F>>
        BlockingAsyncContainersOfMany.ContainerSupplier<F, CollectionContainerOfMany<F, Fs>> containerSupplier(
            CollectionSupplier<F, Fs> collectionSupplier
        ) {
            return () -> new CollectionContainerOfMany<>(collectionSupplier.get());
        }
    }
    
}
