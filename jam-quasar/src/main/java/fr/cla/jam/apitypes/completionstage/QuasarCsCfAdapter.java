package fr.cla.jam.apitypes.completionstage;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class QuasarCsCfAdapter extends CsCfAdapter {

    private final FiberScheduler scheduler;

    public QuasarCsCfAdapter(FiberScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public <T, U> Function<T, CompletableFuture<U>> adapt(
        Function<T, CompletionStage<U>> adaptee
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

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
         Es inputs,
        Function<E, CompletionStage<Fs>> mapper,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return flatMapAdapt(inputs, mapper, this::adapt, collectionSupplier, collectionUnion);
    }

}
