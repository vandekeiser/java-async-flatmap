package fr.cla.jam.blocking;

import fr.cla.jam.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static fr.cla.jam.blocking.BlockingAsyncCollectionsWithGenericAsyncifier.FlatteningCollector.flattening;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;
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
    
    static class FlatteningCollector<F, Fs extends Collection<F>> implements Collector<
        CompletableFuture<Fs>,
        AtomicReference<CompletableFuture<Fs>>, 
        CompletableFuture<Fs>
    > {
        private final CollectionSupplier<F, Fs> collectionSupplier;
        private final BinaryOperator<Fs> collectionUnion;

        private FlatteningCollector(CollectionSupplier<F, Fs> collectionSupplier, BinaryOperator<Fs> collectionUnion) {
            this.collectionSupplier = requireNonNull(collectionSupplier);
            this.collectionUnion = requireNonNull(collectionUnion);
        }

        static <F, Fs extends Collection<F>> 
        Collector<CompletableFuture<Fs>, ?, CompletableFuture<Fs>> flattening(
                CollectionSupplier<F, Fs> collectionSupplier,
                BinaryOperator<Fs> collectionUnion
        ) {
            return new FlatteningCollector<>(collectionSupplier, collectionUnion);
        }
        
        @Override 
        public Supplier<AtomicReference<CompletableFuture<Fs>>> supplier() {
            return () -> new AtomicReference<>(completedFuture(collectionSupplier.get()));
        }
    
        @Override
        public BiConsumer<AtomicReference<CompletableFuture<Fs>>, CompletableFuture<Fs>> accumulator() {
            return (acc, curr) -> acc.accumulateAndGet(curr,
                (cf1, cf2) -> cf1.thenCombine(cf2, collectionUnion)   
            );
        }
    
        @Override
        public BinaryOperator<AtomicReference<CompletableFuture<Fs>>> combiner() {
            return (acc1, acc2) -> new AtomicReference<>(
                acc1.get().thenCombine(acc2.get(), collectionUnion)    
            );
        }
    
        @Override
        public Function<AtomicReference<CompletableFuture<Fs>>, CompletableFuture<Fs>> finisher() {
            return AtomicReference::get;
        }
    
        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.of(UNORDERED, CONCURRENT);
        }
    }
    
}
