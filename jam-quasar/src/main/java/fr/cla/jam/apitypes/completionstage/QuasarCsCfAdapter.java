package fr.cla.jam.apitypes.completionstage;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static fr.cla.jam.util.collectors.FlatteningSetCollector.flattening;
import static java.util.stream.Collectors.toSet;

public class QuasarCsCfAdapter {

    public static <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, CompletionStage<U>> adaptee,
        FiberScheduler scheduler
    ) {
        return input -> {
            CompletableFuture<U> fiberCf = new CompletableFuture<>();

            new Fiber<>(scheduler, () -> {
                adaptee.apply(input).whenComplete((res, x) -> {
                    if (x != null) fiberCf.completeExceptionally(x);
                    else fiberCf.complete(res);
                });
            }).start();

            return fiberCf;
        };
    }

    public static <E, F> CompletableFuture<Set<F>> flatMapAdapt(
            Set<E> inputs,
            Function<E, CompletionStage<Set<F>>> mapper,
            FiberExecutorScheduler dedicatedScheduler
    ) {
        return CsCfAdapter.flatMapAdapt(
            inputs,
            mapper,
            m -> QuasarCsCfAdapter.adapt(m, dedicatedScheduler)
        );
    }

}
