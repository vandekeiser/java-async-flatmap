package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.ContainerOfMany;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningContainerOfManyCollector.flattening;
import static java.util.stream.Collectors.toSet;

/**
 * This class only works if the "container of many" is insensitive to order.
 *  Generalizing it to order-dependant results doesn't seem worth the effort for now,
 *  because most operations that can be optimized by parallelezing them don't require order.
 */
public final class ManySyncCfAdapter {

    /**
     * @param inputs The mapped elements
     * @param mapper The mapping function, which returns many target elements for each source element.
     * @param parallelisationPool The thread pool specifically used to make the flatMap operation parallel.
     * @param containerSupplier Instantiates an empty containers of target elements
     * @param containerUnion Merges 2 containers of target elements, following the deduplication semantics determined by the type of the target container
     * @param <E> Element type of the source of mapper()
     * @param <Es> Container type of the source of mapper()
     * @param <F> Element type of the target of mapper()
     * @param <Fs> Container type of the target of mapper()
     * @return The result of applying the mapper, but flattened to a single level of Container. The mapping operation is launched before returning, but is probably not completed.
     */
    public static <E, Es extends ContainerOfMany<E>, F, Fs extends ContainerOfMany<F>> 
    CompletableFuture<Fs> flatMapAdaptUsingPool(
        Es inputs,
        Function<E, Fs> mapper,
        Executor parallelisationPool,
        ContainerOfMany.ContainerSupplier<F, Fs> containerSupplier,
        BinaryOperator<Fs> containerUnion
    ) {
        return inputs
            //Can do this because ContainerOfMany defines stream()
            .stream()
            //Call the 1->N operation asynchronously    
            .map(CollectionSyncCfAdapter.adaptUsingPool(mapper, parallelisationPool))
            //This Stream terminal operation ensures that 
            // the 1->N mapping operation is launched before returning 
            // (this is required since Stream intermediate operations are lazy)
            .collect(toSet())
            //Go back to the Stream world to be able to use Functional Programming
            .stream() 
            //Merge the partial results in a non-sync way
            // (don't wait for all partial results)
            .collect(flattening(containerSupplier, containerUnion));     
    }

}
