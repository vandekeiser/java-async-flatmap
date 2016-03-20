package fr.cla.jam.util.collectors;

import fr.cla.jam.util.containers.ContainerOfMany;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;

public class FlatteningContainerOfManyCollector<F, Fs extends ContainerOfMany<F>>
implements Collector<
    //The source Stream's element type
    CompletableFuture<Fs>,
    //Need to use an AtomicReference as accumulator
    // since CompletableFuture is not safely mutable once completed
    // (cf. the javadocs of CompletableFuture's complete() and obtrudeValue())
    AtomicReference<CompletableFuture<Fs>>,
    //The collect() return type: same as the element type, so this Collector flattens!
    CompletableFuture<Fs>
> {

    private final ContainerOfMany.ContainerSupplier<F, Fs> containerSupplier;
    private final BinaryOperator<Fs> containerUnion;

    private FlatteningContainerOfManyCollector(ContainerOfMany.ContainerSupplier<F, Fs> containerSupplier, BinaryOperator<Fs> containerUnion) {
        this.containerSupplier = requireNonNull(containerSupplier);
        this.containerUnion = requireNonNull(containerUnion);
    }

    //The only purpose of this method is to make instantiating this collector
    // more readable from the point of view of its caller.
    public static <F, Fs extends ContainerOfMany<F>>
    Collector<CompletableFuture<Fs>, ?, CompletableFuture<Fs>> flattening(
        ContainerOfMany.ContainerSupplier<F, Fs> containerSupplier,
        BinaryOperator<Fs> containerUnion
    ) {
        return new FlatteningContainerOfManyCollector<>(containerSupplier, containerUnion);
    }

    @Override
    public Supplier<AtomicReference<CompletableFuture<Fs>>> supplier() {
        return () -> new AtomicReference<>(completedFuture(containerSupplier.get()));
    }

    @Override
    public BiConsumer<AtomicReference<CompletableFuture<Fs>>, CompletableFuture<Fs>> accumulator() {
        return (acc, curr) -> acc.accumulateAndGet(curr,
            (cf1, cf2) -> cf1.thenCombine(cf2, containerUnion)
        );
    }

    @Override
    public BinaryOperator<AtomicReference<CompletableFuture<Fs>>> combiner() {
        return (acc1, acc2) -> new AtomicReference<>(
            acc1.get().thenCombine(acc2.get(), containerUnion)
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
