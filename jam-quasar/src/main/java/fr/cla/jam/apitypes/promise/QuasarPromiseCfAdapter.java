package fr.cla.jam.apitypes.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class QuasarPromiseCfAdapter {

    private final FiberScheduler dedicatedScheduler;

    public QuasarPromiseCfAdapter(FiberScheduler dedicatedScheduler) {
        this.dedicatedScheduler = Objects.requireNonNull(dedicatedScheduler);
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, Promise<U>> adaptee
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

    public <E, F> CompletableFuture<Set<F>> flatMapAdapt(
        Set<E> inputs,
        Function<E, Promise<Set<F>>> adaptee
    ) {
        return PromiseCfAdapter.flatMapAdapt(inputs, adaptee, this::adapt);
    }

}
