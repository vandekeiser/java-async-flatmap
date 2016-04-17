package fr.cla.jam.util.containers.unused;

import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class CollectionStreamable<E, Es extends Collection<E>>
implements Streamable<E> {

    private final Es coll;

    public CollectionStreamable(Es coll) {
        this.coll = requireNonNull(coll);
    }

    public Es underlyingContainer() {
        return this.coll;
    }

    @Override
    public Stream<E> stream() {
        return underlyingContainer().stream();
    }

    public static <F, Fs extends Collection<F>>
    BinaryOperator<CollectionStreamable<F, Fs>> containerUnion(
            BinaryOperator<Fs> collectionUnion
    ) {
        return (c1, c2) -> new CollectionStreamable<>(collectionUnion.apply(
            c1.underlyingContainer(), c2.underlyingContainer()
        ));
    }

    public static <F, Fs extends Collection<F>>
    Supplier<F, CollectionStreamable<F, Fs>> containerSupplier(
            CollectionSupplier<F, Fs> collectionSupplier
    ) {
        return () -> new CollectionStreamable<>(collectionSupplier.get());
    }
}
