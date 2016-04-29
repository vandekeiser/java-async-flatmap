package fr.cla.jam.apitypes.sync.unused;

import fr.cla.jam.apitypes.sync.SyncCfAdapter;
import fr.cla.jam.util.containers.unused.Streamable;

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

    private final SyncCfAdapter singleResultAdapter;

    public ManySyncCfAdapter(Executor pool) {
        this.singleResultAdapter = new SyncCfAdapter(pool);
    }

    /**
     * @param inputs The mapped elements
     * @param adaptee The mapping function, which returns many target elements for each source element.
     * @param supplier Instantiates an empty containers of target elements
     * @param containerUnion Merges 2 containers of target elements, following the deduplication semantics determined by the type of the target container
     * @param <E> Element type of the source of adaptee()
     * @param <Es> Container type of the source of adaptee()
     * @param <F> Element type of the target of adaptee()
     * @param <Fs> Container type of the target of adaptee()
     * @return The result of applying the adaptee, but flattened to a single level of Container. The mapping operation is launched before returning, but is probably not completed.
     */
    public <E, Es extends Streamable<E>, F, Fs extends Streamable<F>>
    CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, Fs> adaptee,
        Streamable.Supplier<F, Fs> supplier,
        BinaryOperator<Fs> containerUnion
    ) {
        return inputs
            //Can do this because Streamable defines stream()
            .stream()
            //Call the 1->N operation asynchronously    
            .map(singleResultAdapter.toCompletableFuture(adaptee))
            //This Stream terminal operation ensures that 
            // the 1->N mapping operation is launched before returning 
            // (this is required since Stream intermediate operations are lazy)
            .collect(toSet())
            //Go back to the Stream world to be able to use Functional Programming
            .stream() 
            //Merge the partial results in a non-sync way
            // (don't wait for all partial results)
            .collect(flattening(supplier, containerUnion));
    }

}
