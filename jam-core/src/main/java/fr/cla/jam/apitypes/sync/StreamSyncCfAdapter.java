package fr.cla.jam.apitypes.sync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static fr.cla.jam.util.collectors.FlatteningStreamCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class StreamSyncCfAdapter {

    public interface StreamSupplier<E, Es extends Stream<E>> extends Supplier<Es> {}
    
    public static <E, F> CompletableFuture<Stream<F>> adaptUsingPool(
        Stream<E> inputs,
        Function<E, Stream<F>> mapper,
        Executor parallelisationPool
    ) {
        return inputs
            .map(SyncCfAdapter.adaptUsingPool(mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .collect(flattening(Stream::empty, Stream::concat));
    }
    
}
