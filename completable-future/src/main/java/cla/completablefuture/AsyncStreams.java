package cla.completablefuture;

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

import static cla.completablefuture.AsyncStreams.FlatteningAsyncColectionCollector.flattening;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static java.util.stream.Collectors.toSet;

public final class AsyncStreams {

    public interface StreamSupplier<E, Es extends Stream<E>> extends Supplier<Es> {}
    
    public static <E, F> CompletableFuture<Stream<F>> flatMapAsync(
        Stream<E> inputs,
        Function<E, Stream<F>> mapper,
        Executor parallelisationPool
    ) {
        return AsyncStreams.flatMapAsync(inputs, mapper, parallelisationPool, Stream::empty, Stream::concat);
    }
    
    public static <E, Es extends Stream<E>, F, Fs extends Stream<F>> CompletableFuture<Fs> flatMapAsync(
        Es inputs,
        Function<E, Fs> mapper,
        Executor parallelisationPool,
        StreamSupplier<F, Fs> streamSupplier,
        BinaryOperator<Fs> streamUnion
    ) {
        return inputs
            .map(CompletableFutures.asyncify(mapper, parallelisationPool))
            .collect(toSet())
            .stream()
            .collect(flattening(streamSupplier, streamUnion));    
    }
    
    static class FlatteningAsyncColectionCollector<F, Fs extends Stream<F>> implements Collector<
        CompletableFuture<Fs>,
        AtomicReference<CompletableFuture<Fs>>, 
        CompletableFuture<Fs>
    > {
        private final StreamSupplier<F, Fs> streamSupplier;
        private final BinaryOperator<Fs> streamUnion;

        private FlatteningAsyncColectionCollector(StreamSupplier<F, Fs> streamSupplier, BinaryOperator<Fs> streamUnion) {
            this.streamSupplier = requireNonNull(streamSupplier);
            this.streamUnion = requireNonNull(streamUnion);
        }

        static <F, Fs extends Stream<F>> Collector<CompletableFuture<Fs>, ?, CompletableFuture<Fs>> flattening(
            StreamSupplier<F, Fs> streamSupplier, 
            BinaryOperator<Fs> streamUnion
        ) {
            return new FlatteningAsyncColectionCollector<>(streamSupplier, streamUnion);
        }
        
        @Override 
        public Supplier<AtomicReference<CompletableFuture<Fs>>> supplier() {
            return () -> new AtomicReference<>(completedFuture(streamSupplier.get()));
        }
    
        @Override
        public BiConsumer<AtomicReference<CompletableFuture<Fs>>, CompletableFuture<Fs>> accumulator() {
            return (acc, curr) -> acc.accumulateAndGet(curr,
                (cf1, cf2) -> cf1.thenCombine(cf2, streamUnion)   
            );
        }
    
        @Override
        public BinaryOperator<AtomicReference<CompletableFuture<Fs>>> combiner() {
            return (acc1, acc2) -> new AtomicReference<>(
                acc1.get().thenCombine(acc2.get(), streamUnion)    
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
