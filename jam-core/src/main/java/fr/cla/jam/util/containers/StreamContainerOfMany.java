package fr.cla.jam.util.containers;

import fr.cla.jam.apitypes.sync.BlockingAsyncStreams_Factor;

import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class StreamContainerOfMany<E, Es extends Stream<E>>
implements ContainerOfMany<E> {
    
    private final Es stream;

    public StreamContainerOfMany(Es stream) {
        this.stream = requireNonNull(stream);
    }

    public Es underlyingContainer() {
        return this.stream;
    }

    @Override
    public Stream<E> stream() {
        return underlyingContainer();
    }

    public static <F, Fs extends Stream<F>>
    BinaryOperator<StreamContainerOfMany<F, Fs>> containerUnion(
            BinaryOperator<Fs> streamUnion
    ) {
        return (c1, c2) -> new StreamContainerOfMany<>(streamUnion.apply(
            c1.underlyingContainer(), c2.underlyingContainer()
        ));
    }

    public static <F, Fs extends Stream<F>>
    ContainerSupplier<F, StreamContainerOfMany<F, Fs>> containerSupplier(
            BlockingAsyncStreams_Factor.StreamSupplier<F, Fs> streamSupplier
    ) {
        return () -> new StreamContainerOfMany<>(streamSupplier.get());
    }

}
