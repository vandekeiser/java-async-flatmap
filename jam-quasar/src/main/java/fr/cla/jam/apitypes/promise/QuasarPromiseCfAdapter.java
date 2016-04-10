package fr.cla.jam.apitypes.promise;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class QuasarPromiseCfAdapter {

    public static <T, U> Function<
        Function<T, Promise<U>>,
        Function<T, CompletableFuture<U>>
    > usingFiberScheduler(FiberScheduler dedicatedScheduler) {
        return promise -> input -> {
            CompletableFuture<U> fiberCf = new CompletableFuture<>();

            new Fiber<>(dedicatedScheduler, () -> promise.apply(input).whenComplete(
                res -> fiberCf.complete(res),
                x -> fiberCf.completeExceptionally(x)
            )).start();

            return fiberCf;
        };
    }

}
