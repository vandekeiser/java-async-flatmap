package fr.cla.jam.apitypes.completionstage;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class QuasarCsCfAdapter extends CsCfAdapter {

    private final FiberScheduler quasar;

    public QuasarCsCfAdapter(FiberScheduler quasar) {
        this.quasar = quasar;
    }

    public <T, U> Function<T, CompletableFuture<U>> toCompletableFuture(
        Function<T, CompletionStage<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> fiberCf = new CompletableFuture<>();

            new Fiber<>(quasar, () -> {
                adaptee.apply(input).whenComplete((res, x) -> {
                    if (x != null) fiberCf.completeExceptionally(x);
                    else fiberCf.complete(res);
                });
            }).start();

            return fiberCf;
        };
    }

}
