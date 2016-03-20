package fr.cla.jam.apitypes.sync;

import fr.cla.jam.util.containers.StreamContainerOfMany;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class BlockingAsyncStreams_Factor {

    public interface StreamSupplier<E, Es extends Stream<E>> 
    extends Supplier<Es> {}
    
    public static <E, F> CompletableFuture<Stream<F>> flatMapStreamAsync(
        Stream<E> inputs,
        Function<E, Stream<F>> mapper,
        Executor parallelisationPool
    ) {
        return flatMapStreamAsync(inputs, mapper, parallelisationPool, Stream::empty, Stream::concat);    
    }
    
    static <E, Es extends Stream<E>, F, Fs extends Stream<F>> 
    CompletableFuture<Fs> flatMapStreamAsync(
        Es inputs,
        Function<E, Fs> mapper,
        Executor parallelisationPool,
        StreamSupplier<F, Fs> streamSupplier,
        BinaryOperator<Fs> streamUnion
    ) {
        return CollectSyncContainersOfManyApiIntoCf.flatMapAsync(
            new StreamContainerOfMany<>(inputs),
            mapper.andThen(StreamContainerOfMany::new),
            parallelisationPool,
            StreamContainerOfMany.containerSupplier(streamSupplier),
            StreamContainerOfMany.containerUnion(streamUnion)
        ).thenApply(
            StreamContainerOfMany::underlyingContainer
        );
    }


}
