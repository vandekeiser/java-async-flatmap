package fr.cla.jam.nonblocking.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import fr.cla.jam.nonblocking.callback.Callback;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class QuasarifyPromise {

    public static <T, U> Function<
        Function<T, Promise<U>>,
        Function<T, CompletableFuture<U>>
    > usingFiberScheduler(FiberScheduler dedicatedScheduler) {
        return callback -> input -> startWaitingForCallbackInFiberScheduler(callback, input, dedicatedScheduler);
    }

    private static <T, U> CompletableFuture<U> startWaitingForCallbackInFiberScheduler(
        Function<T, Promise<U>> promise,
        T input,
        FiberScheduler dedicatedScheduler
    ) {
        CompletableFuture<U> fiberCf = new CompletableFuture<>();

        new Fiber<>(dedicatedScheduler, () -> promise.apply(input).whenComplete(
            res -> fiberCf.complete(res),
            x -> fiberCf.completeExceptionally(x)
        )).start();

        return fiberCf;
    }

}
