package fr.cla.jam.util.containers.unused;


import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class StreamStreamable<E, Es extends Stream<E>>
implements Streamable<E> {
    
    private final Es stream;

    public StreamStreamable(Es stream) {
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
    BinaryOperator<StreamStreamable<F, Fs>> containerUnion(
        BinaryOperator<Fs> streamUnion
    ) {
        return (c1, c2) -> new StreamStreamable<>(streamUnion.apply(
            c1.underlyingContainer(), c2.underlyingContainer()
        ));
    }

    public static <F, Fs extends Stream<F>>
    Supplier<F, StreamStreamable<F, Fs>> containerSupplier(
        StreamSupplier<F, Fs> streamSupplier
    ) {
        return () -> new StreamStreamable<>(streamSupplier.get());
    }

}
