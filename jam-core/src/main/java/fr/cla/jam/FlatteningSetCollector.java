package fr.cla.jam;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.emptySet;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;

public class FlatteningSetCollector<F> implements Collector<
    CompletableFuture<Set<F>>,
    AtomicReference<CompletableFuture<Set<F>>>,
    CompletableFuture<Set<F>>
> {

    public static <F> Collector<CompletableFuture<Set<F>>, ?, CompletableFuture<Set<F>>> flattening() {
        return new FlatteningSetCollector<>();
    }

    @Override
    public Supplier<AtomicReference<CompletableFuture<Set<F>>>> supplier() {
        return () -> new AtomicReference<>(completedFuture(emptySet()));
    }

    @Override
    public BiConsumer<AtomicReference<CompletableFuture<Set<F>>>, CompletableFuture<Set<F>>> accumulator() {
        return (acc, curr) -> acc.accumulateAndGet(curr,
            (cf1, cf2) -> cf1.thenCombine(cf2, Sets::union)
        );
    }

    @Override
    public BinaryOperator<AtomicReference<CompletableFuture<Set<F>>>> combiner() {
        return (acc1, acc2) -> new AtomicReference<>(
            acc1.get().thenCombine(acc2.get(), Sets::union)
        );
    }

    @Override
    public Function<AtomicReference<CompletableFuture<Set<F>>>, CompletableFuture<Set<F>>> finisher() {
        return AtomicReference::get;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(UNORDERED, CONCURRENT);
    }

}
