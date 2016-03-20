package fr.cla.jam.util.collectors;

import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
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

public class FlatteningCollectionCollector<F, Fs extends Collection<F>> implements Collector<
    CompletableFuture<Fs>,
    AtomicReference<CompletableFuture<Fs>>,
    CompletableFuture<Fs>
> {

    private final CollectionSupplier<F, Fs> collectionSupplier;
    private final BinaryOperator<Fs> collectionUnion;

    private FlatteningCollectionCollector(CollectionSupplier<F, Fs> collectionSupplier, BinaryOperator<Fs> collectionUnion) {
        this.collectionSupplier = requireNonNull(collectionSupplier);
        this.collectionUnion = requireNonNull(collectionUnion);
    }

    public static <F, Fs extends Collection<F>>
    Collector<CompletableFuture<Fs>, ?, CompletableFuture<Fs>> flattening(
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return new FlatteningCollectionCollector<>(collectionSupplier, collectionUnion);
    }

    @Override
    public Supplier<AtomicReference<CompletableFuture<Fs>>> supplier() {
        return () -> new AtomicReference<>(completedFuture(collectionSupplier.get()));
    }

    @Override
    public BiConsumer<AtomicReference<CompletableFuture<Fs>>, CompletableFuture<Fs>> accumulator() {
        return (acc, curr) -> acc.accumulateAndGet(curr,
            (cf1, cf2) -> cf1.thenCombine(cf2, collectionUnion)
        );
    }

    @Override
    public BinaryOperator<AtomicReference<CompletableFuture<Fs>>> combiner() {
        return (acc1, acc2) -> new AtomicReference<>(
            acc1.get().thenCombine(acc2.get(), collectionUnion)
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
