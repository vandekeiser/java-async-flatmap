package fr.cla.jam.util.containers;

import java.util.Collection;
import java.util.function.Supplier;

public interface CollectionSupplier<E, Es extends Collection<E>> extends Supplier<Es> {
}
