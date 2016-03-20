package fr.cla.jam.util.containers;

import java.util.function.Supplier;
import java.util.stream.Stream;

//ContainerOfMany seems a better name than Streamable here
public interface ContainerOfMany<E> {

    Stream<E> stream();

    interface ContainerSupplier<E, Es extends ContainerOfMany<E>> extends Supplier<Es> {}

}
