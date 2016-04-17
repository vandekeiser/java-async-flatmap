package fr.cla.jam.apitypes.completionstage;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class QuasarCsCfAdapter {

    private final FiberScheduler scheduler;

    public QuasarCsCfAdapter(FiberScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(Function<T, CompletionStage<U>> adaptee) {
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

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, CompletionStage<Set<F>>> mapper
    ) {
        return CsCfAdapter.flatMapAdapt(inputs, mapper, this::adapt);
    }

}
