package cla.completablefuture.blocking;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static cla.completablefuture.blocking.BlockingAsyncContainersOfMany.FlatteningCollector.flattening;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static java.util.stream.Collectors.toSet;

/**
 * Async (as in CompletableFuture) operationson monads of type "containers of many", 
 *  which are more general than Stream/Collection.
 *  So far only flatMap is publicly exposed (map will be if a need emerges).
 * 
 * For now, this class only works if the "container of many" is insensitive to order.
 *  A typical use case is when the source and target are Sets,
 *  but this class can cope with the case of a mapping operation that provides a List (or Stream, or other),
 *  or a consumer that requires a List (or Stream, or other).
 *  Generalizing it to order-dependant results doesn't seem worth the effort for now,
 *  because most operations that can be optimized by parallelezing them don't require order
 *  (this is not an absolute and could be done if required).
 */
public final class BlockingAsyncContainersOfMany {
    
    //ContainerOfMany seems a better name than Streamable here,
    // since we're talking about async "containers of many" 
    // (either Stream or Collection) 
    public interface ContainerOfMany<E> { Stream<E> stream(); }
    
    //Only used in this package so far, but keeping this public on principle,
    // Even tough the only containers (that i can think of for now) 
    // for which flatmap has performance aspects 
    // are collective (as opposed to Optional/Try) monads like Stream and Collection.
    // (why doesn't Iterable define stream()?), 
    // users could in principle use custom collective monads.
    public interface ContainerSupplier<E, Es extends ContainerOfMany<E>> 
    extends Supplier<Es> {}

    /**
     * This will rarely be called by classes outside of this package,
     *  but is still provided for (maybe premature..) generality.
     * The mapping operation is launched before returning, but is probably not completed. 
     * 
     * @param inputs The mapped elements
     * @param mapper The mapping function, which returns many target elements for each source element
     * @param parallelisationPool The thread pool specifically used to make the flatMap operation parallel.
     * @param containerSupplier Instantiates an empty containers of target elements
     * @param containerUnion Merges 2 containers of target elements, following the deduplication semantics determined by the type of the target container
     * @param <E> Element type of the source of mapper()
     * @param <Es> Container type of the source of mapper()
     * @param <F> Element type of the target of mapper()
     * @param <Fs> Container type of the target of mapper()
     * @return The result of applying the mapper, but flattened to a single level of Container.
     */
    public static <E, Es extends ContainerOfMany<E>, F, Fs extends ContainerOfMany<F>> 
    CompletableFuture<Fs> flatMapAsync(
        Es inputs,
        Function<E, Fs> mapper,
        Executor parallelisationPool,
        ContainerSupplier<F, Fs> containerSupplier,
        BinaryOperator<Fs> containerUnion
    ) {
        return inputs
            //Can do this because ContainerOfMany defines stream()
            .stream()
            //Call the 1->N operation asynchronously    
            .map(BlockingCompletableFutures.asyncifyWithPool(mapper, parallelisationPool))
            //This Stream terminal operation ensures that 
            // the 1->N mapping operation is launched before returning 
            // (this is required since Stream intermediate operations are lazy)
            .collect(toSet())
            //Go back to the Stream world to be able to use Functional Programming
            .stream() 
            //Merge the partial results in a non-blocking way 
            // (don't wait for all partial results)
            .collect(flattening(containerSupplier, containerUnion));     
    }
    
    static class FlatteningCollector<F, Fs extends ContainerOfMany<F>> 
    implements Collector<
        //The source Stream's element type
        CompletableFuture<Fs>, 
        //Need to use an AtomicReference as accumulator 
        // since CompletableFuture is not safely mutable once completed 
        // (cf. the javadocs of CompletableFuture's complete() and obtrudeValue()) 
        AtomicReference<CompletableFuture<Fs>>,     
        //The collect() return type: same as the element type, so this Collector flattens!
        CompletableFuture<Fs> 
    > { 
        private final ContainerSupplier<F, Fs> containerSupplier;
        private final BinaryOperator<Fs> containerUnion;

        private FlatteningCollector(ContainerSupplier<F, Fs> containerSupplier, BinaryOperator<Fs> containerUnion) {
            this.containerSupplier = requireNonNull(containerSupplier);
            this.containerUnion = requireNonNull(containerUnion);
        }

        //The only purpose of this method is to make instantiating this collector 
        // more readable from the point of view of its caller.
        static <F, Fs extends ContainerOfMany<F>> 
        Collector<CompletableFuture<Fs>, ?, CompletableFuture<Fs>> flattening(
                ContainerSupplier<F, Fs> containerSupplier,
                BinaryOperator<Fs> containerUnion
        ) {
            return new FlatteningCollector<>(containerSupplier, containerUnion);
        }
        
        @Override 
        public Supplier<AtomicReference<CompletableFuture<Fs>>> supplier() {
            return () -> new AtomicReference<>(completedFuture(containerSupplier.get()));
        }
    
        @Override
        public BiConsumer<AtomicReference<CompletableFuture<Fs>>, CompletableFuture<Fs>> accumulator() {
            return (acc, curr) -> acc.accumulateAndGet(curr,
                (cf1, cf2) -> cf1.thenCombine(cf2, containerUnion)   
            );
        }
    
        @Override
        public BinaryOperator<AtomicReference<CompletableFuture<Fs>>> combiner() {
            return (acc1, acc2) -> new AtomicReference<>(
                acc1.get().thenCombine(acc2.get(), containerUnion)    
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
