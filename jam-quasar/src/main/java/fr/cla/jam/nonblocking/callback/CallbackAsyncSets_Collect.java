package fr.cla.jam.nonblocking.callback;

import fr.cla.jam.Sets;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static fr.cla.jam.nonblocking.callback.CallbackAsyncSets_Collect.FlatteningCollector.flattening;
import static java.util.Collections.emptySet;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.UNORDERED;
import static java.util.stream.Collectors.toSet;

public final class CallbackAsyncSets_Collect {

    public static <E, F> CompletableFuture<Set<F>> flatMapCallbackAsync(
        Set<E> inputs,
        BiConsumer<E, Callback<Set<F>>> mapper,
        Function<
            BiConsumer<E, Callback<Set<F>>>,
            Function<E, CompletableFuture<Set<F>>>
        > asyncifier
    ) {
        return inputs.stream()
            .map(asyncifier.apply(mapper))
            .collect(toSet())
            .stream()
            .collect(flattening());
    }
    
    static class FlatteningCollector<F> implements Collector<
        CompletableFuture<Set<F>>,
        AtomicReference<CompletableFuture<Set<F>>>, 
        CompletableFuture<Set<F>>
    > {
        static <F> Collector<CompletableFuture<Set<F>>, ?, CompletableFuture<Set<F>>> flattening() {
            return new FlatteningCollector<>();
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
    
}
