package fr.cla.jam.apitypes.sync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static fr.cla.jam.util.collectors.FlatteningStreamCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class CollectSyncStreamApiIntoCf {

    public interface StreamSupplier<E, Es extends Stream<E>> extends Supplier<Es> {}
    
    public static <E, F> CompletableFuture<Stream<F>> flatMapAsync(
        Stream<E> inputs,
        Function<E, Stream<F>> mapper,
        Executor parallelisationPool
    ) {
        return CollectSyncStreamApiIntoCf.flatMapAsync(inputs, mapper, parallelisationPool, Stream::empty, Stream::concat);
    }
    
    public static <E, Es extends Stream<E>, F, Fs extends Stream<F>> CompletableFuture<Fs> flatMapAsync(
        Es inputs,
        Function<E, Fs> mapper,
        Executor parallelisationPool,
        StreamSupplier<F, Fs> streamSupplier,
        BinaryOperator<Fs> streamUnion
    ) {
        return inputs
            .map(SyncApi2CfApi.asyncifyWithPool(mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .collect(flattening(streamSupplier, streamUnion));    
    }

}
