package fr.cla.jam.apitypes.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class QuasarPromiseCfAdapter {

    public static <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, Promise<U>> adaptee,
        FiberScheduler dedicatedScheduler
    ) {
        return input -> {
            CompletableFuture<U> fiberCf = new CompletableFuture<>();

            new Fiber<>(dedicatedScheduler, () -> adaptee.apply(input).whenComplete(
                res -> fiberCf.complete(res),
                x -> fiberCf.completeExceptionally(x)
            )).start();

            return fiberCf;
        };
    }

    public static <E, F> CompletableFuture<Set<F>> adaptFlatMap(
        Set<E> inputs,
        Function<E, Promise<Set<F>>> adaptee,
        FiberExecutorScheduler scheduler
    ) {
        return PromiseCfAdapter.adaptFlatMap(inputs, adaptee, mapper -> adapt(mapper, scheduler));
    }

}
