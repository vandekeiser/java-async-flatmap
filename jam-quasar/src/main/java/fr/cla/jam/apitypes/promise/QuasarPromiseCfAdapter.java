package fr.cla.jam.apitypes.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.apitypes.callback.Callback;
import fr.cla.jam.util.containers.CollectionSupplier;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class QuasarPromiseCfAdapter extends PromiseCfAdapter{

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

    public <E, Es extends Collection<E>, F, Fs extends Collection<F>> CompletableFuture<Fs> flatMapAdapt(
        Es inputs,
        Function<E, Promise<Fs>> adaptee,
        CollectionSupplier<F, Fs> collectionSupplier,
        BinaryOperator<Fs> collectionUnion
    ) {
        return flatMapAdapt(inputs, adaptee, this::adapt, collectionSupplier, collectionUnion);
    }

}
