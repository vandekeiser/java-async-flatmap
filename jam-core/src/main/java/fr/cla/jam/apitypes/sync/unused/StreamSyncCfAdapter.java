package fr.cla.jam.apitypes.sync.unused;

import fr.cla.jam.apitypes.sync.PoolSingleResultSyncCfAdapter;
import fr.cla.jam.apitypes.sync.SingleResultSyncCfAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.cla.jam.util.collectors.FlatteningStreamCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class StreamSyncCfAdapter {

    private final PoolSingleResultSyncCfAdapter singleResultAdapter;

    public StreamSyncCfAdapter(Executor pool) {
        this.singleResultAdapter = new PoolSingleResultSyncCfAdapter(pool);
    }

    public <E, F> CompletableFuture<Stream<F>> adaptUsingPool(
        Stream<E> inputs,
        Function<E, Stream<F>> mapper
    ) {
        return inputs
            .map(singleResultAdapter.adaptUsingPool(mapper))
            .collect(toSet())
            .stream()
            .collect(flattening(Stream::empty, Stream::concat));
    }
    
}
