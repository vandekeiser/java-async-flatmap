package fr.cla.jam.apitypes.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class QuasarPromiseCfAdapter extends PromiseCfAdapter{

    private final FiberScheduler quasar;

    public QuasarPromiseCfAdapter(FiberScheduler quasar) {
        this.quasar = Objects.requireNonNull(quasar);
    }

    public <T, U> Function<T, CompletableFuture<U>> toCompletableFuture(
        Function<T, Promise<U>> adaptee
    ) {
        return input -> {
            CompletableFuture<U> cf = new CompletableFuture<>();
            new Fiber<>(quasar, () -> adaptee.apply(input).then(
                success -> cf.complete(success),
                failure -> cf.completeExceptionally(failure)
            )).start();
            return cf;
        };
    }

}
