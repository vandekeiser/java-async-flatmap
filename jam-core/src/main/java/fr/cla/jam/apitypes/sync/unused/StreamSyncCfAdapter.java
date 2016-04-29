package fr.cla.jam.apitypes.sync.unused;

import fr.cla.jam.apitypes.sync.SyncCfAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.cla.jam.util.collectors.FlatteningStreamCollector.flattening;
import static java.util.stream.Collectors.toSet;

public final class StreamSyncCfAdapter {

    private final SyncCfAdapter singleResultAdapter;

    public StreamSyncCfAdapter(Executor pool) {
        this.singleResultAdapter = new SyncCfAdapter(pool);
    }

    public <E, F> CompletableFuture<Stream<F>> adapt(
        Stream<E> inputs,
        Function<E, Stream<F>> adaptee
    ) {
        return inputs
            .map(singleResultAdapter.toCompletableFuture(adaptee))
            .collect(toSet())
            .stream()
            .collect(flattening(Stream::empty, Stream::concat));
    }
    
}
