package fr.cla.jam.util.containers.unused;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface StreamSupplier<E, Es extends Stream<E>> extends Supplier<Es> {
}
