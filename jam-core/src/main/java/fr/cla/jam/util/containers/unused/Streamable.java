package fr.cla.jam.util.containers.unused;

import java.util.stream.Stream;

public interface Streamable<E> {

    Stream<E> stream();

    interface Supplier<E, Es extends Streamable<E>> extends java.util.function.Supplier<Es> {}

}
