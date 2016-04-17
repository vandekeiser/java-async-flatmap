package fr.cla.jam.apitypes.sync.unused;

import fr.cla.jam.apitypes.sync.SyncCfAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.cla.jam.util.collectors.FlatteningStreamCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class StreamSyncCfAdapter extends SyncCfAdapter {

    public static <E, F> CompletableFuture<Stream<F>> adaptUsingPool(
        Stream<E> inputs,
        Function<E, Stream<F>> mapper,
        Executor parallelisationPool
    ) {
        return inputs
            .map(adaptUsingPool(mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .collect(flattening(Stream::empty, Stream::concat));
    }
    
}
