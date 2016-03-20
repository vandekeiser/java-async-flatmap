package fr.cla.jam.util.containers;

import java.util.Collection;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class CollectionContainerOfMany<E, Es extends Collection<E>>
implements ContainerOfMany<E> {

    private final Es coll;

    public CollectionContainerOfMany(Es coll) {
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
    BinaryOperator<CollectionContainerOfMany<F, Fs>> containerUnion(
            BinaryOperator<Fs> collectionUnion
    ) {
        return (c1, c2) -> new CollectionContainerOfMany<>(collectionUnion.apply(
            c1.underlyingContainer(), c2.underlyingContainer()
        ));
    }

    public static <F, Fs extends Collection<F>>
    ContainerSupplier<F, CollectionContainerOfMany<F, Fs>> containerSupplier(
            CollectionSupplier<F, Fs> collectionSupplier
    ) {
        return () -> new CollectionContainerOfMany<>(collectionSupplier.get());
    }
}
