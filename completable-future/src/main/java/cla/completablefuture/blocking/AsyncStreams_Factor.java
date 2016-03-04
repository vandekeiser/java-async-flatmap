package cla.completablefuture.blocking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class AsyncStreams_Factor {

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
        return AsyncContainersOfMany.flatMapAsync(
            new StreamContainerOfMany<>(inputs),
            mapper.andThen(StreamContainerOfMany::new),
            parallelisationPool,
            StreamContainerOfMany.containerSupplier(streamSupplier),
            StreamContainerOfMany.containerUnion(streamUnion)
        ).thenApply(
            StreamContainerOfMany::underlyingContainer
        );
    }
    
    
    private static class StreamContainerOfMany<E, Es extends Stream<E>> 
    implements AsyncContainersOfMany.ContainerOfMany<E> {
        private final Es stream;
        private StreamContainerOfMany(Es stream) {this.stream = requireNonNull(stream);}
        private Es underlyingContainer() {return this.stream;}
        
        @Override public Stream<E> stream() {
            return underlyingContainer();
        }
        
        static <F, Fs extends Stream<F>> 
        BinaryOperator<StreamContainerOfMany<F, Fs>> containerUnion(
            BinaryOperator<Fs> streamUnion
        ) {
            return (c1, c2) -> new StreamContainerOfMany<>(streamUnion.apply(
                c1.underlyingContainer(), c2.underlyingContainer()
            ));           
        }
        
        static <F, Fs extends Stream<F>>
        AsyncContainersOfMany.ContainerSupplier<F, StreamContainerOfMany<F, Fs>> containerSupplier(
            StreamSupplier<F, Fs> streamSupplier
        ) {
            return () -> new StreamContainerOfMany<>(streamSupplier.get());
        }
    }
    
}
